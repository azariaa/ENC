import nltk
import sys
from nltk.stem.porter import *
from nltk.corpus import wordnet as wn
from bs4 import BeautifulSoup
from helperFunctions import *

# Given the input query, this method returns the qWord and the type of question
def DefineQuestionType(question):
    if len(question.strip())==0:
        return "Nope",""
    if question[-1] == ",":
        question = question[0:-1]
    questionTypeWH = ["how many", "who", "what", "where", "when", "why", "which", "how"]
    questionTypeFactoid1 = ["do", "did", "does"]
    questionTypeFactoid2 = ["is", "are", "has", "had", "was", "were", "would", "will", "should", "can", "could"]
    questionTypeOther = ["how", "list", "describe"]
    allTypes = ("WHType", "YesNo", "List", "None")
    questionLC = question.lower()
    # Check first word in sentence
    wordsInSentence = questionLC.split()
    if wordsInSentence[0] in questionTypeWH:
        # Check for How Many type question
        if wordsInSentence[0]+" "+wordsInSentence[1] in questionTypeWH:
            return allTypes[0], "how many"
        # All other WH Questions
        else:
            return allTypes[0], wordsInSentence[0]

    elif wordsInSentence[0] in questionTypeFactoid2:
        return allTypes[1], wordsInSentence[0]

    elif wordsInSentence[0] in questionTypeFactoid1:
        return allTypes[1], wordsInSentence[0]

    elif wordsInSentence[0] in questionTypeOther:
        return allTypes[2], wordsInSentence[0]

    else:
        # For complex sentences, check for question words after comma
        if "," in questionLC:
            wordsInSentence = questionLC.split(",")[1].split()
            if wordsInSentence[0] in questionTypeWH:
                # Check for How many
                if wordsInSentence[0]+" "+wordsInSentence[1] in questionTypeWH:
                    return allTypes[0], "how many"
                # All other WH Questions
                else:
                    return allTypes[0], wordsInSentence[0]

            elif wordsInSentence[0] in questionTypeFactoid2:
                return allTypes[1], wordsInSentence[0]

            elif wordsInSentence[0] in questionTypeFactoid1:
                return allTypes[1], wordsInSentence[0]

            elif wordsInSentence[0] in questionTypeOther:
                return allTypes[2], wordsInSentence[0]
            else:
                return allTypes[3], ""
        #As a last resort, look for question word in the entire question sentence
        #We ignore edge cases where there are multiple question words
        else: 
            return "None", "Nope"

def getStopWords(fileLocation):
    stopwords = set()
    for line in open(fileLocation):
        stopwords.add(line.rstrip('\n')) 
    return stopwords

def checkForStopWordRatio(questionSentence, stopwords):
    questionSentence = questionSentence.replace("?","")
    totalWords = 0
    stopWordsCount = 0
    for word in questionSentence.split():
        totalWords+=1
        if word.strip() in stopwords:
            stopWordsCount+=1
    return float(stopWordsCount)/float(totalWords)

def removeStopWordsFromSentence(sentence, stopwords):
    finalSentence = ""
    sentence = sentence.replace("?","").replace(".","").replace(",","")
    for word in sentence.split():
        if word.lower() not in stopwords:
            finalSentence = finalSentence + word + " "
    return finalSentence

def extractQuestions(rawQueryFile, stopWordsFiles):
    rawQuery = convertHTMLtoRawText(rawQueryFile)
    querySentences = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?|\n|\,)\s', rawQuery)
    stopwords = getStopWords(stopWordsFiles)
    for idx,sentence in enumerate(querySentences):
        sentence = sentence.strip()
        if len(sentence) > 0:
            qType, qWord = DefineQuestionType(sentence)
            if qType != "None" and qType != "Nope":
                if checkForStopWordRatio(sentence, stopwords) <= 0.5:
                    print sentence, "~~~", removeStopWordsFromSentence(sentence, stopwords)
                else:
                    if (idx-1) >=0 and DefineQuestionType(querySentences[idx-1]) == ("None",""):
                        print querySentences[idx-1] + " " + sentence, "~~~", removeStopWordsFromSentence(querySentences[idx-1] + " " + sentence, stopwords)
                    elif (idx+1) <= len(querySentences) and DefineQuestionType(querySentences[idx+1]) == ("None",""):
                        print sentence + " " + querySentences[idx+1], "~~~", removeStopWordsFromSentence(sentence + " " + querySentences[idx+1], stopwords)

rawQueryFile = sys.argv[1]
stopWordsFiles = sys.argv[2]
extractQuestions(rawQueryFile, stopWordsFiles)