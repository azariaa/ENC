#!/usr/bin/python
'''
Author: Suruchi Shah
<Explain the purpose and functionality of script>
Usage: python extractEmailContent.py [emailFile]
'''
import sys
import os
import pprint
import json

class EmailContent(object):
	def __init__(self):
		self.Init()

	def Init(self):
		self.emailFile = sys.argv[1]
		self.sourceFile = sys.argv[2]
		self.emailDictionary = []
		self.sourceDomainDictionary = {}
		self.populateSourceDomainDictionary()

	def getRawText(self):
		with open(self.emailFile, "r") as f:
			content = "\n".join(line.rstrip() for line in f)

		# Split on </email> to separate out the emails
		emailRawTexts = []
		emailRawTexts = content.split("</EMAIL>")

		# For each email, extract the individual pieces of data
		for emailRaw in emailRawTexts:
			if len(emailRaw.strip()) > 1:
				self.extractEmailInformation(emailRaw)

		self.generateFinalOutputJSON()
	
	def extractEmailInformation(self, emailRaw):
		# Remove <email> tag
		emailRaw = emailRaw.replace("<EMAIL>", "")
		contentLines = emailRaw.split("\n")
		emailContent = {}
		for line in contentLines:
			if len(line.strip()) < 2:
				continue
			
			if line.startswith("EMAILID:"):
				emailContent["EMAILID"] = line.replace("EMAILID:", "")

			elif line.startswith("FROM:"):
				emailContent["SENDER"] = line.replace("FROM:", "")
				emailContent["SENDERSOURCE"] = self.getEmailSource(line)
			
			elif line.startswith("RECIPIENT:"):
				emailContent["RECIPIENT"] = line.replace("RECIPIENT:", "")
			
			elif line.startswith("RECEIVEDATE:"):
				emailContent["RECEIVEDATE"] = line.replace("RECEIVEDATE:", "")
			
			elif line.startswith("SENTDATE:"):
				emailContent["SENTDATE"] = line.replace("SENTDATE:", "")
		
			elif line.startswith("SUBJECT:"):
				emailContent["SUBJECT"] = line.replace("SUBJECT:", "")

			elif line.startswith("CONTENT:"):
				emailContent["RAWCONTENT"] = line.replace("RAWCONTENT:", "")
				emailContent["TYPE"] = self.getType(emailContent)

			else:
				emailContent["RAWCONTENT"] = emailContent["RAWCONTENT"] + "\n" + line

		emailContent["CONTENT"] = self.getRelevantContent(emailContent)
		self.emailDictionary.append(emailContent)

	# This method opens the file containing source domain and populates the dictionary with the information
	def populateSourceDomainDictionary(self):
		with open(self.sourceFile, "r") as f:
			content = f.readlines()

		for line in content:
			line = line.strip()
			key, value = line.split("=")
			self.sourceDomainDictionary[value] = key

	# This method extracts the domain source of the email and returns the key
	def getEmailSource(self, senderText):
		domain = senderText[senderText.index("@")+1:senderText.index("com")-1]
		for key, value in self.sourceDomainDictionary.iteritems():
			if key in domain:
				return value

	# This method defines the flow of logic to get the type of email depending on source
	def getType(self, emailContent):
		if emailContent["SENDERSOURCE"] == "PIAZZA":
			return self.getPiazzaType(emailContent["RAWCONTENT"])
		else:
			return "UNKNOWN"


	# This method gets the type of Piazza Email (whether it is a question, note, post or unknown)
	def getPiazzaType(self, contentText):
		contentLines = contentText.split("\n")
		piazzaType = ""
		for line in contentLines:
			if "question" in line.lower():
				piazzaType = "QUESTION"
			elif "note" in line.lower():
				piazzaType = "NOTE"
			elif "post" in line.lower():
				piazzaType = "POST"
			else:
				piazzaType = "UNKNOWN"
			return piazzaType

	# This method defines the flow of logic to get the relevant content of email depending on source
	def getRelevantContent(self, emailContent):
		if (emailContent["SENDERSOURCE"] == "PIAZZA" and emailContent["TYPE"] != "UNKNOWN"):
			return self.getPiazzaRelevantContent(emailContent["RAWCONTENT"])
		else:
			return emailContent["RAWCONTENT"]

	# This method gets the relevant content for Piazza emails
	def getPiazzaRelevantContent(self, contentText):
		contentText = contentText[contentText.index("CONTENT:"):contentText.index("Go to http")]
		# We remove the first line from the piazza email because the first line contains text such as "Your classmate posted a new question"
		sansfirstline = '\n'.join(contentText.split('\n')[1:]) 
		return sansfirstline

	# This method simply converts the dictionary into JSON and prints the final output
	def generateFinalOutputJSON(self):
		jsonarray = json.dumps(self.emailDictionary, ensure_ascii=False)
		print "{\"AllEmails\":" + jsonarray + "}"


test = EmailContent()
test.getRawText()