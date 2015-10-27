from subprocess import *
from nltk.corpus import wordnet as wn
from nltk.stem.porter import *
from pprint import pprint
from bs4 import BeautifulSoup
import nltk

# Method that takes a sentence, tokenizes and returns a list of tokens, posTags for the tokens and the morphedTokens
def tokenize(sentence):
    tokens = nltk.word_tokenize(sentence)
    processedTokens = []
    for token in tokens:
        if "-" in token:
            processedTokens.extend(token.split("-"))
        elif ")" in token or "(" in token:
            token = token.replace("(","").replace(")","")
            processedTokens.append(token)
        else:
            processedTokens.append(token)
    tokens = processedTokens
    posTags = nltk.pos_tag(tokens)
    morphTokens = [wn.morphy(token) for token in tokens]
    #If morphed tokens are None, we use the original token
    morphTokens = [token.decode('utf-8').encode('ascii', 'ignore').lower() if token is not None else tokens[i].lower() for (i,token) in enumerate(morphTokens)]
    return tokens, posTags, morphTokens

# Method that converts HTML text into Raw text using beautiful soup
def convertHTMLtoRawText(fileLocation):
    markup = open(fileLocation)
    soup = BeautifulSoup(markup, "html.parser")
    content = re.sub(r'[^\x00-\x7F]+',' ', soup.get_text())
    return str(content.encode('utf-8').decode('ascii', 'replace'))

# Method that takes in a word, original word and outputs the antonyms set for the word
# Author: Suruchi Shah
def antonyms_as_set(input_word, original_word):
    antonyms = get_antonyms_as_set(input_word)
    if (original_word is not None) and (antonyms is None or len(antonyms)==0):
        return get_antonyms_as_set(original_word)

    return antonyms

# Method that takes in a word and outputs the antonyms set for the word
# Author: Suruchi Shah
def get_antonyms_as_set(input_word):
    if input_word is None:
        return set()

    antonyms = set()
    synonyms = wn.synsets(input_word)
    
    for syn in synonyms:
        lemmas = syn.lemmas()
        
        for lem in lemmas:
            for ant in lem.antonyms():
                if wn.morphy(ant.name()) is not None:
                    antonyms.add(str(wn.morphy(ant.name()).encode('utf-8').decode('ascii', 'ignore')))
    return antonyms

# Method that takes in a word, original word and outputs the synonyms set for the word
# Author: Suruchi Shah
def synonyms_as_set(input_word, original_word):
    synonyms = get_synonyms_as_set(input_word)
    if (original_word is not None) and (synonyms is None or len(synonyms)==0):
        return get_synonyms_as_set(original_word)

    return synonyms

# Method that takes in a word, original word and outputs the synonyms set for the word
# Author: Suruchi Shah
def get_synonyms_as_set(input_word):
    if input_word is None:
        return set()

    synonyms = set()
    synSets = wn.synsets(input_word)
    for syn in synSets:
        for lemma_name in syn.lemma_names():
            if wn.morphy(lemma_name) is not None:
                synonyms.add(str(wn.morphy(lemma_name).encode('utf-8').decode('ascii','ignore')))
    return synonyms

# Calculates Levenshtein Distance between two strings
# Source: Wikipedia
# Modified By: Suruchi Shah
def levenshtein(s1, s2):
    if len(s1) < len(s2):
        return levenshtein(s2, s1)

    # len(s1) >= len(s2)
    if len(s2) == 0:
        return len(s1)
    
    previous_row = range(len(s2) + 1)
    for i, c1 in enumerate(s1):
        current_row = [i + 1]
        for j, c2 in enumerate(s2):
            insertions = previous_row[j + 1] + 1 # j+1 instead of j since previous_row and current_row are one character longer
            deletions = current_row[j] + 1     # than s2
            substitutions = previous_row[j] + (c1 != c2)
            current_row.append(min(insertions, deletions, substitutions))
        previous_row = current_row
    return previous_row[-1]