import sys, string, os, collections, urllib, time, datetime

from xml.etree import ElementTree as ET

from google.appengine.ext import webapp
from google.appengine.ext import db
from google.appengine.ext import blobstore
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext.webapp import template


#A custom tag is used to render most of HR-XML data into HTML tables.
#The custom tag is located in /customtags/mytags.py.
webapp.template.register_template_library('customtags.mytags') 


#The Datastore class to store HR-XML Resumes.
class resumeXML(db.Model):
	name = db.StringProperty()
	id = db.StringProperty()
	resumeDate = db.DateTimeProperty()
	blobXML = db.BlobProperty()
	blobImage = db.BlobProperty()
	blobImageContentType = db.StringProperty()
	dateAdded = db.DateTimeProperty(auto_now_add=True)


#This function is used to deal with the recursive nature of the 
#Qualifications/Competency schema from HR-XML. 'e' is the 
#Qualifications node.
def recurseThroughTree(e):
	parent = []
	for e2 in e.findall('Competency'):
		if e.get('name') != None:
			parent.append(e2.get('name'))
			child = recurseThroughTree(e2) #Recursion! Yes!
			if child != None: 
				parent.append(child)
		else:
			return None
	if len(parent):
		return parent
	else: #This may be a leaf
		e3 = e.find('CompetencyEvidence')
		if ET.iselement(e3): #Leaves may have these extra data elements.
			e4 = e3.find('NumericValue')
			if ET.iselement(e4):
				Detail = [e3.get('lastUsed') + ' ' + 
					e4.get('description') + ' ' +  e4.text + '/' + 
					e4.get('maxValue')]
				return Detail
	return None #This should never be called.


#This function gets the string date from the FlexibleDateType.
#It returns a date as a string.
def getFlexibleDate(e, strXpath):
	if ET.iselement(e) and isinstance(strXpath, str):
		e2 = e.find(strXpath + '/*')
		if ET.iselement(e2):
			return e2.text
	return None
	

#This function goes one level deep to find children tags and their
#text. It accepts an element and returns a dictionary of the format
#tag: text.
def getChildrenAsList(e, strXpath):
	if ET.iselement(e) and isinstance(strXpath, str): #Test input.
		childrenDict = {}
		for e2 in e.iterfind(strXpath + '/*'):
			if e2.text: #Hopefully this doesn't break.
				childrenDict[e2.tag] = e2.text
		return childrenDict
	return None
	

#This function deals with formatting a DeliveryAddress node.
#It returns a list of address lines. It's a hack, but it
#should be sufficient for this assignment.
def getDeliveryAddress(e):
	if ET.iselement(e) and e.tag == 'DeliveryAddress':
		DeliveryAddress = []
		testStr = e.text #There has to be an easier way to test.
		if testStr and len(testStr.strip()):
			DeliveryAddress.append(testStr)
		for AddressLine in e.findall('AddressLine'):
			testStr = AddressLine.text
			if testStr and len(testStr.strip()):
				DeliveryAddress.append(testStr)
		if e.findtext('BuildingNumber') and e.findtext('StreetName'):
			AddressLine = []
			AddressLine.append(e.findtext('BuildingNumber'))
			AddressLine.append(e.findtext('StreetName'))
			testStr = ' '.join(AddressLine)
			if testStr and len(testStr.strip()):
				DeliveryAddress.append(testStr)
		testStr = e.findtext('Unit')
		if testStr and len(testStr.strip()):
			DeliveryAddress.append('Unit ' + testStr)
		testStr = e.findtext('PostOfficeBox')
		if testStr:
			DeliveryAddress.append('P.O. Box ' + testStr)
		return DeliveryAddress
	return None


#Handles most of the work of preparing to render the resume.
class MainPage(webapp.RequestHandler):	
	def get(self, resource):

		#Load element tree object.
		tree = ET.ElementTree()
		
		#Check if a Datastore object needs to be retrieved.
		resource = str(urllib.unquote(resource)).rstrip('#')		
		if len(resource): #Test input. It should be a db.key.
			m = resumeXML()
			m = db.get(resource)
			if m: #We found a Datastore object, load it into a tree.
				node = ET.fromstring(str(m.blobXML))
			else: #An object was not found, load the default.
				node = tree.parse(open("templates/resume2.xml"))
		else: #Nothing was passed, render the default.
			node = tree.parse(open("templates/resume2.xml"))

		#Remove the namespace.
		for elem in node.getiterator():
			if elem.tag[0] == '{': elem.tag = elem.tag.split('}',1)[1]

		#The following big chunk of code retrieves data from the HR-XML Resume
		#instance. The implementation is not perfect and there could be many
		#instances that will break the script. However, for the purpose of the
		#assignment, this meets or exceed the criteria.		
		
		#Get ID
		ResumeID = node.findtext('.//ResumeId/IdValue')
		RevisionDate = node.findtext('.//RevisionDate')

		#Get the person's name
		PersonName = node.findtext('.//PersonName/FormattedName')
		PersonName = PersonName.title()
		
		#Get contact information
		e = node.find('.//StructuredXMLResume/ContactInfo/ContactMethod')
		ContactInformation = collections.OrderedDict()
		ContactInformation['StreetAddress'] = getDeliveryAddress(e.find('PostalAddress/DeliveryAddress'))
		ContactInformation['City'] = e.findtext('PostalAddress/Municipality')
		ContactInformation['State'] = e.findtext('PostalAddress/Region')
		ContactInformation['Zip'] = e.findtext('PostalAddress/PostalCode')
		ContactInformation['Telephone'] = getChildrenAsList(e, 'Telephone')
		ContactInformation['Fax'] = e.findtext('Fax/FormattedNumber')
		ContactInformation['EmailAddress'] = e.findtext('InternetEmailAddress')
		
		#Get executive summary
		ExecutiveSummary = node.findtext('.//ExecutiveSummary')
		
		#Get objective
		Objective = node.findtext('.//Objective')
		
		#Get EmploymentHistory
		EmploymentHistory = []
		for e in node.findall('.//EmploymentHistory/EmployerOrg'):
			link = {}
			link['LinkName'] = e.findtext('EmployerOrgName')
			link['InternetDomainName'] = e.findtext('InternetDomainName')
			link['Description'] = e.findtext('PositionHistory/Description')
			child = collections.OrderedDict()
			child['EmployerOrgName'] = link
			child['Title'] = e.findtext('PositionHistory/Title')
			child['StartDate'] = getFlexibleDate(e, 'PositionHistory/StartDate')
			child['EndDate'] = getFlexibleDate(e, 'PositionHistory/EndDate')
			EmploymentHistory.append(child)

		#Get EducationHistory
		EducationHistory = []
		for e in node.findall('.//EducationHistory/SchoolOrInstitution'):
			link = {}
			link['LinkName'] = e.findtext('School/SchoolName')
			link['InternetDomainName'] = e.findtext('School/InternetDomainName')
			link['Description'] = e.findtext('PositionHistory/Description')
			child = collections.OrderedDict()
			child['SchoolName'] = link
			child['DegreeName'] = e.findtext('Degree/DegreeName')
			child['Major'] = e.findtext('Major')
			child['DegreeDate'] = getFlexibleDate(e, 'Degree/DegreeDate')
			EducationHistory.append(child)	  

		#Get LicensesAndCertifications
		LicensesAndCertifications = []
		for e in node.findall('.//LicensesAndCertifications/LicenseOrCertification'):
			link = {}
			link['LinkName'] = e.findtext('Name')
			link['InternetDomainName'] = None
			link['Description'] = e.findtext('Description')
			child = collections.OrderedDict()
			child['Name'] = link
			child['Id'] = e.findtext('Id')
			child['IssuingAuthority'] = e.findtext('IssuingAuthority')
			child['ValidTo'] = getFlexibleDate(e, 'EffectiveDate/ValidTo')
			LicensesAndCertifications.append(child)
			
		#Get MilitaryHistory
		MilitaryHistory = []
		for e in node.findall('.//MilitaryHistory'):
			repeatChild = e.findtext('CountryServed')
			for e2 in e.findall('ServiceDetail'):
				link = {}
				link['LinkName'] = e2.get('branch')
				link['InternetDomainName'] = None
				link['Description'] = e2.findtext('AreaOfExpertise')
				child = collections.OrderedDict()
				child['Country'] = repeatChild
				child['Branch'] = link
				child['Rank'] = e2.findtext('RankAchieved/CurrentOrEndRank')
				child['ServiceDates'] = []
				child['ServiceDates'].append(getFlexibleDate(e2, 'DatesOfService/StartDate'))
				child['ServiceDates'].append(getFlexibleDate(e2, 'DatesOfService/EndDate'))
				child['DischargeStatus'] = e2.findtext('DischargeStatus')
				child['Awards'] = []
				for e3 in e2.findall('RecognitionAchieved'):
					child['Awards'].append(e3.text.title())
				child['Campaigns'] = []
				for e3 in e2.findall('Campaign'):
					child['Campaigns'].append(e3.text)
				MilitaryHistory.append(child)

		#Get PatentHistory
		PatentHistory = []
		for e in node.findall('.//PatentHistory/Patent'):
			link = {}
			link['LinkName'] = e.findtext('PatentTitle')
			link['InternetDomainName'] = e.findtext('Link')
			link['Description'] = e.findtext('Description')
			child = collections.OrderedDict()
			child['PatentTitle'] = link
			child['Status'] = e.findtext('PatentDetail/PatentMilestone/Status')
			child['Date'] = e.findtext('PatentDetail/PatentMilestone/Date')
			PatentHistory.append(child)
		
		#Get PublicationHistory
		PublicationHistory = []
		for e in node.findall('.//PublicationHistory/*'):
			link = {}
			link['LinkName'] = e.findtext('Title')
			link['InternetDomainName'] = e.findtext('Link')
			link['Description'] = e.findtext('Abstract')
			child = collections.OrderedDict()
			child['Title'] = link
			if e.tag == 'OtherPublication':
				child['Type'] = e.get('type')
			else:
				child['Type'] = e.tag
			if e.find('PublicationDate'):
				publicationDate = getFlexibleDate(e, 'PublicationDate')
			elif e.find('ConferenceDate/AnyDate'):
				publicationDate = getFlexibleDate(e, 'ConferenceDate')
			child['PublicationDate'] = publicationDate
			PublicationHistory.append(child)

		#Get SpeakingEventHistory
		SpeakingEventHistory = []
		for e in node.findall('.//SpeakingEventsHistory/SpeakingEvent'):
			link = {}
			link['LinkName'] = e.findtext('EventName')
			link['InternetDomainName'] = e.findtext('Link')
			link['Description'] = e.findtext('Description')
			child = collections.OrderedDict()
			child['EventName'] = link
			child['StartDate'] = getFlexibleDate(e, 'StartDate')
			child['Title'] = e.findtext('Title')
			child['EventType'] = e.findtext('EventType')
			SpeakingEventHistory.append(child)
			
		#Get Qualifications
		Qualifications = []
		for e in node.findall('.//Qualifications/Competency'):
			Qualifications.append(e.get('name'))
			child = recurseThroughTree(e)
			if child != None: Qualifications.append(child)
			
		#Get Languages
		Languages = []
		for e in node.findall('.//Languages/Language'):
			child = collections.OrderedDict()
			child['LanguageCode'] = e.findtext('LanguageCode')
			child['Read'] = e.findtext('Read')
			child['Write'] = e.findtext('Write')
			child['Speak'] = e.findtext('Speak')
			child['Comments'] = e.findtext('Comments')
			Languages.append(child)

		#Get Achievements
		Achievements = []
		for e in node.findall('.//Achievements/Achievement'):
			child = collections.OrderedDict()
			child['Date'] = getFlexibleDate(e, 'Date')
			child['Description'] = e.findtext('Description')
			child['IssuingAuthority'] = e.findtext('IssuingAuthority')
			Achievements.append(child)

		#Get Associations
		Associations = []
		for e in node.findall('.//Associations/Association'):
			link = {}
			link['LinkName'] = e.findtext('Name')
			link['InternetDomainName'] = e.findtext('Link')
			link['Description'] = e.findtext('Comments')
			child = collections.OrderedDict()
			child['Name'] = link
			child['StartDate'] = e.findtext('StartDate/YearMonth')
			child['Role'] = e.findtext('Role/Name')
			Associations.append(child)

		#Get References
		References = []
		for e in node.findall('.//References/Reference'):
			link = {}
			link['LinkName'] = e.findtext('PersonName/FormattedName')
			emailAddress = e.findtext('ContactMethod/InternetEmailAddress')
			if emailAddress:
				link['InternetDomainName'] = 'mailto:' + emailAddress
			else: link['InternetDomainName'] = None
			link['Description'] = e.findtext('Comments')
			child = collections.OrderedDict()
			child['Name'] = link
			child['Type'] = e.get('type')
			child['PositionTitle'] = e.findtext('PositionTitle')
			child['Telephone'] = e.findtext('ContactMethod/Telephone/FormattedNumber')
			References.append(child)

		#Get SecurityCredentials
		SecurityCredentials = []
		for e in node.findall('.//SecurityCredentials/SecurityCredential'):
			link = {}
			link['LinkName'] = e.findtext('Name')
			link['InternetDomainName'] = None
			link['Description'] = e.findtext('Description')
			child = collections.OrderedDict()
			child['Name'] = link
			child['IssuingAuthority'] = e.findtext('IssuingAuthority')
			child['FirstIssued'] = getFlexibleDate(e, 'EffectiveDate/FirstIssuedDate')
			child['ValidTo'] = getFlexibleDate(e, 'EffectiveDate/ValidTo')
			SecurityCredentials.append(child)


		#Since the upload URL may change, it is set in the controller.
		upload_url = '/uploadResume'


		#Test to see which image to display: stored, dummy, or default.
		if 'm' in locals() and isinstance(m, resumeXML):
			if m.blobImage: #We found a stored photo.
				resume_photo = "/serveImg/" + str(m.key())
			else:  #No stored photo, display a dummy image.
				resume_photo = "/static/transparent_pixel.gif"
		else: #We are not looking at a model, just show the default.
			resume_photo = "/static/da1.gif"

		#Get a list of uploaded resumes.
		resumes = resumeXML.gql("ORDER BY resumeDate")


		#Whoa this is a lot of data for the template.
		template_values = {
			'ResumeID': ResumeID,
			'PersonName': PersonName,
			'RevisionDate': RevisionDate,
			'ContactInformation': ContactInformation,
			'ExecutiveSummary': ExecutiveSummary,
			'Objective': Objective,
			'EmploymentHistory': EmploymentHistory,
			'EducationHistory': EducationHistory,
			'LicensesAndCertifications': LicensesAndCertifications,
			'MilitaryHistory': MilitaryHistory,
			'PatentHistory': PatentHistory,
			'PublicationHistory': PublicationHistory,
			'SpeakingEventHistory': SpeakingEventHistory,
			'Qualifications': Qualifications,
			'Languages': Languages,
			'Achievements': Achievements,
			'Associations': Associations,
			'References': References,
			'SecurityCredentials': SecurityCredentials,
			'upload_url': upload_url,
			'resumes': resumes,
			'resume_photo': resume_photo,
		}

		#Fill the template with data and display it.
		path = os.path.join(os.path.dirname(__file__), 'templates/resume2.html')
		self.response.out.write(template.render(path, template_values))


#Accepts an HR-XML Resume (required) and a photo (optional).
class UploadHandler(webapp.RequestHandler):    
	def post(self):
		blobXML = self.request.get('file')
		node = ET.fromstring(str(blobXML)) #Load the XML to test it.

		#Remove the namespace.
		for elem in node.getiterator():
			if elem.tag[0] == '{': elem.tag = elem.tag.split('}',1)[1]

		if ET.iselement(node) and node.findtext('.//PersonName/FormattedName'):
			m = resumeXML() #Test was ok. Create the Datastore object.
			m.blobXML = blobXML
			m.name = node.findtext('.//PersonName/FormattedName')
			m.id = node.findtext('.//ResumeId/IdValue')
			timestring = node.findtext('.//RevisionDate')
			time_format = "%Y-%m-%d"
			m.resumeDate = datetime.datetime.fromtimestamp(
				time.mktime(time.strptime(timestring, time_format)))
			photo = self.request.POST['photo'] 
			if hasattr(photo, 'type'): #Test to see if a photo was uploaded.
				m.blobImage = photo.value
				m.blobImageContentType = photo.type
			m.put() #Put the object in the Datastore
			self.redirect('/resume2/%s' % m.key()) #Show the uploaded resume.
		else: #If something was wrong the person should get an error msg, 
			  #but I'll just redirect for now.
			self.redirect('/resume2') 


#Removes a resume from the Datastore.
class RemoveHandler(webapp.RequestHandler):
	def get(self, resource):
		resource = str(urllib.unquote(resource))		
		if len(resource): #Test the input.
			db.delete(resource)


#Serves an image from the Datastore.
class ImageHandler(webapp.RequestHandler):
	def get(self, resource):
		resource = str(urllib.unquote(resource))		
		if len(resource): #Test the input. Resource should be a db.key.
			m = resumeXML()
			m = db.get(resource)
			if m.blobImage: #Test to see if anything was retrieved.
				self.response.headers['Content-Type'] = m.blobImageContentType
				self.response.out.write(m.blobImage) #Send the image.
		else: #Display something.
			self.redirect('/static/transparent_pixel.gif')
		
		
#Serves the XML from the Datastore to a person wanting to download it.
class DownloadHandler(webapp.RequestHandler):
	def get(self, resource):
		resource = str(urllib.unquote(resource))
		if len(resource): #Test the input. Resource should be a db.key.
			m = resumeXML()
			m = db.get(resource)
			if m.blobXML: #Test to see if anything was retrieved.
				self.response.headers['Content-Type'] = 'text/xml'
				self.response.headers['Content-Disposition'] = "attachment; filename=resume.xml"
				self.response.out.write(m.blobXML) #Send the XML to the requestor.
			else: 
				self.redirect('/')
		else:
			self.redirect('/')
			

application = webapp.WSGIApplication(
	[('/resume2/?(.*)', MainPage),
	 ('/uploadResume', UploadHandler),
	 ('/removeResume/?(.*)', RemoveHandler),
	 ('/serveImg/?(.*)', ImageHandler),
	 ('/serveResume/?(.*)', DownloadHandler),
	], debug=True)
 
def main():
	run_wsgi_app(application)
    
if __name__ == '__main__':
	main()
