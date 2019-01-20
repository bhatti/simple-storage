import unittest
import requests
import logging
import os
from os.path import abspath, isabs, isdir, isfile, join
import random
import string
import sys
import mimetypes
import urllib2
import httplib
import time
import re
import tempfile

BASE_URL = "http://localhost:8080/api"
def random_string (length):
    return ''.join (random.choice (string.letters) for ii in range (length + 1))

class FunctionalTest(unittest.TestCase):
    def upload_file(self, path):
        assert isabs (path)
        assert isfile (path)
        server = '{}/funapp/funjob'.format(BASE_URL)
        logging.debug ('Uploading %r to %r', path, server)
        #
        data = {'labels': 'funlabel1, funlabel2',
                'author': 'bhatti',
                'overwrite': 'true',
                'location': 'office',}

        filename = 'myfunfile' #os.path.basename(path)
        multipart_form_data = {
            'file': (filename, open(path, 'rb'), 'text/plain'),
            'temperature': ('', str(8883)),
        }
        resp = requests.post(server, data = data, files=multipart_form_data)
        logging.info ('Uploaded %r, resp %s', path, resp)
        return resp

    def test_an_upload(self):
        fd, path = tempfile.mkstemp()
        try:
            tmp = os.fdopen(fd, 'w')
            tmp.write('beginmyfile')
            tmp.write(random_string(2000))
            tmp.write('endmyfile')
            tmp.close()
            resp = self.upload_file(path)
            self.assertTrue('funapp' in resp.text)
            self.assertEqual(resp.status_code, 200)
        finally:
            os.remove(path)

    def test_get(self):
        server = '{}/funapp/funjob/myfunfile'.format(BASE_URL)
        response = urllib2.urlopen(server)
        data = response.read()
        self.assertTrue('funapp' in data)

    def test_download(self):
        server = '{}/funapp/funjob/myfunfile/download'.format(BASE_URL)
        response = urllib2.urlopen(server)
        data = response.read()
        self.assertTrue('beginmyfile' in data)

    def test_properties(self):
        data = {'api_option':'paste', 'api_paste_format':'python'}
        server = '{}/funapp/funjob/myfunfile'.format(BASE_URL)
        resp = requests.put(server, data = data)
        self.assertTrue('funapp' in resp.text)
        self.assertEqual(resp.status_code, 200)

if __name__ == '__main__':
    unittest.main()
