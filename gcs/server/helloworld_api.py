"""Hello World API implemented using Google Cloud Endpoints.

Defined here are the ProtoRPC messages needed to define Schemas for methods
as well as those methods defined in an API.
"""
import endpoints
from protorpc import messages
from protorpc import message_types
from protorpc import remote

from google.appengine.ext import db

from gae_python_gcm.gcm import GCMMessage, GCMConnection

# TODO: Replace the following lines with client IDs obtained from the APIs
# Console or Cloud Console.
WEB_CLIENT_ID = '582351471583-mletrplmqgqu0k2ht1oni6kddorajf92.apps.googleusercontent.com'
ANDROID_CLIENT_ID = '582351471583-h35mllon44q8lit3h6kgf4247s3lr5bk.apps.googleusercontent.com'
IOS_CLIENT_ID = 'replace this with your iOS client ID'
ANDROID_AUDIENCE = WEB_CLIENT_ID


package = 'Hello'


#The Datastore class to store HR-XML Resumes.
class resumeXML(db.Model):
	name = db.StringProperty()
	id = db.StringProperty()
	resumeDate = db.DateTimeProperty()
	blobXML = db.BlobProperty()
	blobImage = db.BlobProperty()
	blobImageContentType = db.StringProperty()
	dateAdded = db.DateTimeProperty(auto_now_add=True)


class Greeting(messages.Message):
    """Greeting that stores a message."""
    message = messages.StringField(1)


class GreetingCollection(messages.Message):
    """Collection of Greetings."""
    items = messages.MessageField(Greeting, 1, repeated=True)

class UploadFile(messages.Message):
    messages = messages.BytesField(1)


STORED_GREETINGS = GreetingCollection(items=[
    Greeting(message='hello world!'),
    Greeting(message='goodbye world!'),
])

@endpoints.api(name='helloworld', version='v1',
               allowed_client_ids=[WEB_CLIENT_ID, ANDROID_CLIENT_ID,
                                   IOS_CLIENT_ID],
               audiences=[ANDROID_AUDIENCE])
class HelloWorldApi(remote.Service):
    """Helloworld API v1."""

    MULTIPLY_METHOD_RESOURCE = endpoints.ResourceContainer(
            Greeting,
            times=messages.IntegerField(2, variant=messages.Variant.INT32,
                                        required=True))

    @endpoints.method(MULTIPLY_METHOD_RESOURCE, Greeting,
                      path='hellogreeting/{times}', http_method='POST',
                      name='greetings.multiply')
    def greetings_multiply(self, request):
        return Greeting(message=request.message * request.times)

    @endpoints.method(message_types.VoidMessage, GreetingCollection,
                      path='hellogreeting', http_method='GET',
                      name='greetings.listGreeting')
    def greetings_list(self, unused_request):
        return STORED_GREETINGS

    ID_RESOURCE = endpoints.ResourceContainer(
            message_types.VoidMessage,
            id=messages.IntegerField(1, variant=messages.Variant.INT32))

    @endpoints.method(ID_RESOURCE, Greeting,
                      path='hellogreeting/{id}', http_method='GET',
                      name='greetings.getGreeting')
    def greeting_get(self, request):
        try:
            push_token = 'dT3u3O6QBNk:APA91bEdvRfSa_Qd3KhOFQJ2yZTGIFZQgivgYOFxDVUp84izpRSFToFSDJ_l0r1qAz7YFtEriRDbIQzA0hPfQjX_oxHXiEkWe1yJCEwEC6MWRRX3maNyF4imrzG32vUzUv-7HDmMCow-'
            android_payload = {'your-key': 'your-value'}
            gcm_message = GCMMessage(push_token, android_payload)
            gcm_conn = GCMConnection()
            gcm_conn.notify_device(gcm_message)
            #if(request.id!=99):
            return STORED_GREETINGS.items[request.id]
            #else:
            #   return Greeting(message=blobstore.create_upload_url('/upload_file'))
        except (IndexError, TypeError):
            raise endpoints.NotFoundException('Greeting %s not found.' %
                                              (request.id,))

    @endpoints.method(message_types.VoidMessage, Greeting,
                      path='hellogreeting/authed', http_method='POST',
                      name='greetings.authed')
    def greeting_authed(self, request):
        current_user = endpoints.get_current_user()
        email = (current_user.email() if current_user is not None
                 else 'Anonymous')
        return Greeting(message='hello %s' % (email,))



APPLICATION = endpoints.api_server([HelloWorldApi])