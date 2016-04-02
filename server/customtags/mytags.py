import string

from google.appengine.ext import webapp
from django.utils.encoding import force_unicode
from django.utils.html import conditional_escape
from django.utils.safestring import mark_safe

#Required for any custom filter or tag.
register = webapp.template.create_template_register() 


def hrxmlTable(value, autoescape=None):
    """
    Iterates through a list extracted by python from HR-XML Schemas and creates
    a basic table. If there is an inner dictionary with a URL, this filter will
    will add the link.

    Mark your input as |safe when you use this filter.

    Example: {{ EmploymentHistory|hrxmlTable|safe }}
    """

    #This is necessary to keep the output from escaping the HTML.		
    if autoescape:
        from django.utils.html import conditional_escape
        escaper = conditional_escape
    else:
        escaper = lambda x: x

    #This function does a couple things. It first gets rid of
    #excessive white space sometimes found in blocks of text
    #within XML files. The second and third bit of code cause
    #the string to be changed from a NoneType to an empty string
    #or, in the case of a link, a # so links don't necessarily
    #refresh a page. If NoneTypes are left in the mix, a bunch
    #of 'None''s appear in the output.
    def processString(strVal, isLink=False):
        if strVal is not None:
            strList = string.split(strVal)
            strVal = ' '.join(strList)
        if strVal is None and isLink:
            return '#'
        elif strVal is None:
            return ''
        return escaper(force_unicode(strVal))

    #This function formats a link into acceptable input for
    #an anchor tag's href.
    def processLink(strLink):
        if 'mailto:' in strLink: #formatted for email, ok
            return strLink
        elif '@' in strLink: #needs mailto:
            return 'mailto:' + strLink
        elif '://' in strLink: #formatted for some protocol, ok
            return strLink
        #else add http://
        return 'http://' + strLink

    #This is the main function of the filter. I could explain this,
    #but then I'd have to kill you.
    def _helper(list_, tabs=1):
        indent = u'\t' * tabs
        output = []

        rowStart = '\n<tr>'
        rowEnd = '</tr>\n'

        if list_:

            output.append('<table>')
            output.append(rowStart)
            for key in list_[0].keys():
                header = '\n%s<th>%s</th>\n' % (indent, key)
                output.append(header)

            output.append(rowEnd)

            for i in list_:
                output.append(rowStart)
                for j,k in i.items():
                    output.append('<td>')
                    if isinstance(k, dict) and 'LinkName' in k.keys():
                        strLink = processLink(processString(
                            k['InternetDomainName'],True))
                        output.append('<a href="%s" title="%s">%s</a>' %
                                      (strLink,
                                       processString(k['Description']),
                                       processString(k['LinkName'])))
                    elif isinstance(k, dict):
                        for m,n in k:
                            output.append(processString(n) + '<br/>\n')
                    elif isinstance(k, list):
                        for m in k:
                            output.append(processString(m) + '<br/>\n')
                    else:
                        output.append(processString(k))
                    output.append('</td>')
                output.append(rowEnd)
            output.append('</table>')
            return '\n'.join(output)
        else: #The list was empty.
            return '\n<p>Sorry, nothing was included in this section.</p>'

    #Return a nice HTML table.
    return mark_safe(_helper(value))

hrxmlTable.is_safe = True
hrxmlTable.needs_autoescape = True
 
register.filter(hrxmlTable) 
