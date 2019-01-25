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
import json

BASE_URL = "http://localhost:8080/api"
def random_string (length):
    return ''.join (random.choice (string.letters) for ii in range (length + 1))

class FunctionalTest(unittest.TestCase):
    uploaded_artifact = {}
    def upload_file(self, path):
        assert isabs (path)
        assert isfile (path)
        subsys = 'mysubsys' #random_string(30)
        server = '{}/myorg/mysystem/{}'.format(BASE_URL, subsys)
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
            tmp.write(random_string(5000))
            tmp.write('endmyfile')
            tmp.close()
            resp = self.upload_file(path)
            self.assertTrue('myorg' in resp.text)
            self.assertEqual(resp.status_code, 200)
            FunctionalTest.uploaded_artifact = json.loads(resp.text)
        finally:
            os.remove(path)

    def test_get(self):
        server = '{}/{}'.format(BASE_URL, FunctionalTest.uploaded_artifact["id"])
        resp = urllib2.urlopen(server)
        data = resp.read()
        self.assertTrue('myorg' in data)

    def test_query(self):
        server = '{}?organization=myorg'.format(BASE_URL)
        resp = urllib2.urlopen(server)
        data = resp.read()
        self.assertTrue('myorg' in data)

    def test_query_fail(self):
        server = '{}?organization=xmyorg'.format(BASE_URL)
        resp = urllib2.urlopen(server)
        data = resp.read()
        self.assertFalse('myorg' in data)

    def test_download(self):
        server = '{}/{}/download'.format(BASE_URL, FunctionalTest.uploaded_artifact["id"])
        resp = urllib2.urlopen(server)
        data = resp.read()
        self.assertTrue('beginmyfile' in data)

    def test_save_properties(self):
        data = {'api_option':'paste', 'jxprop': 'jxvalue', 'api_paste_format':'python', 'labels': 'brandnewlabels'}
        server = '{}/{}'.format(BASE_URL, FunctionalTest.uploaded_artifact["id"])
        resp = requests.put(server, data = data)
        print("id %s, data %s, saved %s", FunctionalTest.uploaded_artifact["id"], data, resp.text)
        self.assertTrue('myorg' in resp.text)
        self.assertTrue('jxprop' in resp.text)
        self.assertEqual(resp.status_code, 200)
        #
        server = '{}?organization=myorg&labels=brandnew'.format(BASE_URL)
        resp = urllib2.urlopen(server)
        data = resp.read()
        self.assertTrue('myorg' in data)


if __name__ == '__main__':
    unittest.main()
