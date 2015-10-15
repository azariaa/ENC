'''
Author: Suruchi Shah
Script to take string content and find closest matching sentences to the query
Usage: python extractMatchingSentences.py [rawQueryFile] [contentFile]
'''

import nltk
import sys
from nltk.stem.porter import *

def checkForMatch(content, rawQuery, stemmer):
    result = ''
    contentSentences = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?)\s', content)
    # Replace EOL characters
    rawQuery = rawQuery.replace("\n", "")
    queryTokens = []
    for word in rawQuery.split(" "):
        queryTokens.append(str(stemmer.stem(word)))
    
    lengthOfQuery = len(queryTokens)

    for sentence_level1 in contentSentences:
        for sentence in sentence_level1.split("\n"):
            sentence = sentence.replace("<p>","").replace("</p>","")
            sentence_processed = sentence
            sentence_processed = sentence_processed.decode('ascii','ignore')
            sentence_processed = sentence_processed.replace(". ","").replace("<a href"," <a href")
            sentenceTokens = []
            for word in sentence_processed.split(" "):
                sentenceTokens.append(stemmer.stem(word).decode('ascii'))

            counter = 0.0
            for word in queryTokens:
                if word in sentenceTokens:
                    counter+=1.0
            matchPercent = float(counter/lengthOfQuery)
            if matchPercent >= 0.4:
                result = result + sentence + "\n"
    print result

rawQueryFile = sys.argv[1]
contentFile = sys.argv[2]
rawQuery = ''
content = ''
with open(rawQueryFile, "r") as ins:
    for line in ins:
        rawQuery = rawQuery + line

with open(contentFile, "r") as ins:
    for line in ins:
        content = content + line

stemmer = PorterStemmer()
checkForMatch (content, rawQuery, stemmer)