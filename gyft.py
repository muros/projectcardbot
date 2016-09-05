import sys, httplib, hashlib, time

apiKey = sys.argv[1]
apiSecret = sys.argv[2]
#timestamp = str(long(round(time.time())))
print "Current timestamp:" + str(long(round(time.time())))
timestamp = sys.argv[3]
stringToSign = apiKey + apiSecret + timestamp
print "string to sign:" + stringToSign
signature =  hashlib.sha256(stringToSign).hexdigest()
print signature
print hashlib.sha256(stringToSign)
host = "apitest.gyft.com"
path = "/mashery/v1/reseller/account?api_key=%s&sig=%s" % (apiKey, signature)
conn = httplib.HTTPSConnection(host)
conn.putrequest("GET", path)
conn.putheader("x-sig-timestamp", timestamp)
conn.endheaders()
response = conn.getresponse()
data = response.read()
print data
conn.close()
