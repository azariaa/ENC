'''
Author: Suruchi Shah
Script to take string content and find closest matching sentences to the query
Usage: python extractMatchingSentences.py [rawQueryFile] [contentFile]
'''

import nltk
import os
import sys
from nltk.stem.porter import *
from nltk.corpus import wordnet as wn
from bs4 import BeautifulSoup
from helperFunctions import *
from nltk.tag.stanford import StanfordNERTagger

class AnsweringModule:
    def __init__(self, rawQueryFile, contentFile):
        self.rawQuery = ''
        self.content = ''
        self.questionTypeWH = ["how many", "who", "what", "where", "when", "why", "which", "how"]
        self.questionTypeFactoid1 = ["do", "did", "does"]
        self.questionTypeFactoid2 = ["is", "are", "has", "have", "had", "was", "were", "would", "will", "should", "can", "could"]
        self.questionTypeOther = ["how", "list", "describe"]
        self.stopWords = ("the","a","an","am","of","by","at","be","on","or","any","in","to","as","its","it")
        self.negationWords = ("none", "not", "no", "can't", "couldn't", "don't", "won't","neither","nobody","nowhere","nothing")
        self.allTypes = ("WHType", "YesNo", "List", "None")
        self.cur_dir = os.getcwd()
        self.NERTaggerObj = StanfordNERTagger(self.cur_dir+'/PythonScripts/stanford-ner-2014-06-16/classifiers/english.all.3class.distsim.crf.ser.gz',self.cur_dir+'/PythonScripts/stanford-ner-2014-06-16/stanford-ner.jar')
        self.qWord = ""
        self.Initialize()
        self.Main()

    def Initialize(self):
        # STEP 0: Convert HTML to Raw text using Beautiful Soup
        self.rawQuery = convertHTMLtoRawText(rawQueryFile)
        self.content = convertHTMLtoRawText(contentFile)
        # Replace EOL character
        self.rawQuery = self.rawQuery.strip().replace("?","").replace('"','').replace(".", " ")
        self.campusLocations = {}

    # Replaces the stopwords and the qWord from the question                    
    def ReplaceStopQuestionWords(self, question):
        for stopWord in self.stopWords:
            pattern = r'\b%s\b'% stopWord
            question= re.sub(pattern,"",question,flags=re.IGNORECASE)

        pattern = r'\b%s\b' % self.qWord
        questionWithOutJunk = re.sub(pattern,"",question,flags = re.IGNORECASE)
        
        pattern = r'?$'
        questionWithOutJunk  = questionWithOutJunk.replace('?','')
        
        return questionWithOutJunk

    # Given the input question, this method returns the qWord and the type of question
    def DefineQuestionType(self, question):
        questionLC = question.lower()
        # Check first word in sentence
        wordsInSentence = questionLC.split()
        if wordsInSentence[0] in self.questionTypeWH:
            # Check for How Many type question
            if wordsInSentence[0]+" "+wordsInSentence[1] in self.questionTypeWH:
                return self.allTypes[0], "how many"
            # All other WH Questions
            else:
                return self.allTypes[0], wordsInSentence[0]

        elif wordsInSentence[0] in self.questionTypeFactoid2:
            return self.allTypes[1], wordsInSentence[0]

        elif wordsInSentence[0] in self.questionTypeFactoid1:
            return self.allTypes[1], wordsInSentence[0]

        elif wordsInSentence[0] in self.questionTypeOther:
            return self.allTypes[2], wordsInSentence[0]
        
        else:
            # For complex sentences, check for question words after comma
            if "," in questionLC:
                wordsInSentence = questionLC.split(",")[1].split()
                if wordsInSentence[0] in self.questionTypeWH:
                    # Check for How many
                    if wordsInSentence[0]+" "+wordsInSentence[1] in self.questionTypeWH:
                        return self.allTypes[0], "how many"
                    # All other WH Questions
                    else:
                        return self.allTypes[0], wordsInSentence[0]

                elif wordsInSentence[0] in self.questionTypeFactoid2:
                    return self.allTypes[1], wordsInSentence[0]

                elif wordsInSentence[0] in self.questionTypeFactoid1:
                    return self.allTypes[1], wordsInSentence[0]

                elif wordsInSentence[0] in self.questionTypeOther:
                    return self.allTypes[2], wordsInSentence[0]
                else:
                    return self.allTypes[3], ""
            #As a last resort, look for question word in the entire question sentence
            #We ignore edge cases where there are multiple question words
            else: 
                for q in self.questionTypeWH: 
                    if q in questionLC: return self.allTypes[0], q
                for q in self.questionTypeFactoid2: 
                    if q in questionLC: return self.allTypes[1], q
                for q in self.questionTypeFactoid1: 
                    if q in questionLC: return self.allTypes[1], q
                for q in self.questionTypeOther: 
                    if q in questionLC: return self.allTypes[2], q
                return self.allTypes[3], ""

    # Given a sentence, checks whether it contains any GHC locations
    def CheckForWHEREAnswer(self, sentence):
        for token in sentence:
            if token.lower() in self.campusLocations:
                return True
        sentTokens = nltk.word_tokenize(sentence)
        NERtags = self.NERTaggerObj.tag(sentTokens)
        NERtags = chunkNEROutput(NERtags)
        countOfOccurence = 0
        for i in xrange(0,len(NERtags)):
            if 'LOCATION' in NERtags[i]:
                ans, tag = NERtags[i]
                if ans in sentence:
                    countOfOccurence += 1
                    continue
                return True
        if countOfOccurence > 1:
            return True
        return None

    # Given a sentence, checks whether it contains time stamps
    def CheckForWHENAnswer(self, sentence):
        timeStamp = getTimeStamp(sentence)
        if timeStamp is not None:
            return True
        # Covered Edge case for "second century AD"
        for timeEvent in {"AD", "BCE", "BC"}:
            pattern = r'[^a-zA-Z]%s[^a-zA-Z]' % timeEvent
            if re.search(pattern, sentence) is not None:
                return True
        return None


    # Given the content and question, this method extracts the matching sentences
    def CheckForMatch(self, content, question):
        result = ''

        # Tokenize the question
        queryTokens, queryPosTags, queryMorphTokens = tokenize(question)
        lengthOfQuery = len(queryMorphTokens)
        
        # Split the entire content into Sentences
        contentSentences = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?|\n)\s', content)

        for idx, sentence_level1 in enumerate(contentSentences):
            for sentence in sentence_level1.split("\n"):
                sentence = sentence.replace("<p>","").replace("</p>","").replace(".","")
                sentence_processed = sentence
                sentence_processed = sentence_processed.replace(". ","").replace("<a href"," <a href")
                sentTokens, sentPosTags, sentMorphTokens = tokenize(sentence_processed)
                counter = 0.0
                for word in queryMorphTokens:
                    # Get list of synonyms of the query word
                    synonyms = synonyms_as_set(word, queryTokens[queryMorphTokens.index(word)])

                    # For each word in the query, we check if the word occurs in the sentence
                    if word in sentMorphTokens:
                        counter+=1.0
                    else: # OR check if the word is a synonym of a word in the sentence i.e. one of the synonyms exist in the sentence
                        for syn in synonyms:
                            if syn in sentMorphTokens:
                                counter+=1.0
                                break

                matchPercent = float(counter/lengthOfQuery)
                if matchPercent >= 0.4:
                    # ADDITIONAL CHECKS:
                    # WHERE TYPE QUESTIONS SHOULD HAVE LOCATION IN SENTENCE:
                    if self.qWord == "where":
                        if self.CheckForWHEREAnswer(sentence_processed) == None:
                            # Check for neighboring sentences to extract the location
                            if (idx+1) <= len(contentSentences)-1:
                                fwdSentence = contentSentences[idx+1]
                                if self.CheckForWHEREAnswer(fwdSentence) == True:
                                    sentence = sentence + "\n" + fwdSentence
                            if (idx-1) >= 0:
                                bckSentence = contentSentences[idx-1]
                                if self.CheckForWHEREAnswer(bckSentence) == True:
                                    sentence = bckSentence + "\n" + sentence

                    # WHEN TYPE QUESTIONS SHOULD HAVE TIME IN SENTENCE:
                    if self.qWord == "when":
                        if self.CheckForWHENAnswer(sentence_processed) == None:
                            # If the current sentence doesn't have a date. We check for previous sentence
                            if (idx+1) <= len(contentSentences)-1:
                                fwdSentence = contentSentences[idx+1]
                                if self.CheckForWHENAnswer(fwdSentence) == True:
                                    sentence = sentence + "\n" + fwdSentence
                            if (idx-1) >= 0:
                                bckSentence = contentSentences[idx-1]
                                if self.CheckForWHENAnswer(bckSentence) == True:
                                    sentence = bckSentence + "\n" + sentence

                    result = result + sentence + "\n"
        print result

    def Main(self):
        # STEP 1: Define Question Type: 
        qType, self.qWord = self.DefineQuestionType(self.rawQuery)

        if self.qWord == "where":
            self.campusLocations = getCampusLocation(self.cur_dir+"/PythonScripts/gazeteer/campusLocations.txt")

        # STEP 2: Remove stop words and junk from the question
        questionWithOutJunk = self.ReplaceStopQuestionWords(self.rawQuery)

        # STEP 3:
        self.CheckForMatch(self.content, questionWithOutJunk)


rawQueryFile = sys.argv[1]
contentFile = sys.argv[2]
task = AnsweringModule(rawQueryFile, contentFile)
