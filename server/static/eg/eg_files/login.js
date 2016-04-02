
function ReadCookie(name)
{
   var allCookie, cookieVal, length, start, end;
   cookieVal="";
   name=name+"=";
   allCookie=document.cookie;
   length=allCookie.length;
   if (length>0)
   {
      start=allCookie.indexOf(name, 0);
      if (start!=-1)
	{
         start+=name.length;
   	   end=allCookie.indexOf(";",start);
	   if (end==-1) {end=length;}
	   cookieVal=unescape(allCookie.substring(start,end));
      }
   }
   return(cookieVal);
}

function WriteCookie(name,value,domain,path,expires,secure)
{
   var CookieVal, CookError;
   CookieVal=CookError="";
   if (name)
   {
      CookieVal=CookieVal+escape(name)+"=";
      if (value)
      {
         CookieVal=CookieVal+escape(value);
	   if (domain)
         {
   	      CookieVal=CookieVal+"; domain="+domain;
	   }
         if (path)
	   {
		CookieVal=CookieVal+"; path="+path;
	   }
  	   if (expires)
	   {
	      CookieVal=CookieVal+"; expires="+expires.toGMTString();
         }
         if (secure)
	   {
		CookieVal=CookieVal+"; secure="+secure;
	   }
	}
      else
      {
         CookError=CookError+"Value failure";
	}
   }
   else
   {
      CookError=CookError+"Name failure";
   }
   if (!CookError)
   {
	document.cookie=CookieVal;  // sets the cookie
      if (value != ReadCookie(name))
      {
	   CookError="Write failure";
	}
   }
   return CookError;
}

function DeleteCookie (name,domain,path)
{
   var expireDate=new Date(1);
   if (ReadCookie(name))
   {
      WriteCookie(name, " ", domain, path, expireDate);
   }
}

if (ReadCookie("ebaysignin") == "in")
{
   document.write("<img src=\"http://pics.ebay.com/aw/pics/home/home_myebay_map_out.gif\" width=450 height=15 alt=\"Home, My eBay, Site Map, Sign In/Out\" border=0 usemap=\"#home_myebay_map_hasJS\" align=\"right\"><br clear=\"all\">");
}
else
{
   document.write("<img src=\"http://pics.ebay.com/aw/pics/home/home_myebay_map_in.gif\" width=450 height=15 alt=\"Home, My eBay, Site Map, Sign In/Out\" border=0 usemap=\"#home_myebay_map_hasJS\" align=\"right\"><br clear=\"all\">");
}
