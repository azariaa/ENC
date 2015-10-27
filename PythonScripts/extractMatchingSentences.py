'''
Author: Suruchi Shah
Script to take string content and find closest matching sentences to the query
Usage: python extractMatchingSentences.py [rawQueryFile] [contentFile]
'''

import nltk
import sys
from nltk.stem.porter import *
from nltk.corpus import wordnet as wn
from bs4 import BeautifulSoup
from helperFunctions import *

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
        self.Initialize()
        self.Main()

    def Initialize(self):
        # STEP 0: Convert HTML to Raw text using Beautiful Soup
        self.rawQuery = convertHTMLtoRawText(rawQueryFile)
        self.content = convertHTMLtoRawText(contentFile)
        # Replace EOL character
        self.rawQuery = self.rawQuery.strip().replace("?","").replace('"','')

    # Replaces the stopwords and the qWord from the question                    
    def replaceStopQuestionWords(self, question, qWord):
        for stopWord in self.stopWords:
            pattern = r'\b%s\b'% stopWord
            question= re.sub(pattern,"",question,flags=re.IGNORECASE)

        pattern = r'\b%s\b' % qWord
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

    def CheckForMatch(self, content, question):
        result = ''

        # Tokenize the question
        queryTokens, queryPosTags, queryMorphTokens = tokenize(question)
        lengthOfQuery = len(queryMorphTokens)
        
        # Split the entire content into Sentences
        contentSentences = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?)\s', content)

        for sentence_level1 in contentSentences:
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
                    result = result + sentence + "\n"
        print result

    def Main(self):
        # STEP 1: Define Question Type: 
        qType, qWord = self.DefineQuestionType(self.rawQuery)

        # STEP 2: Remove stop words and junk from the question
        questionWithOutJunk = self.replaceStopQuestionWords(self.rawQuery, qWord)

        # STEP 3:
        self.CheckForMatch(self.content, questionWithOutJunk)


rawQueryFile = sys.argv[1]
contentFile = sys.argv[2]
task = AnsweringModule(rawQueryFile, contentFile)
