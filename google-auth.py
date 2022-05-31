#!/usr/bin/env python

'''
This script will attempt to open your webbrowser,
perform OAuth 2.0 authentication and print your access token.
To install dependencies from PyPI:
  $ pip install oauth2client
Then run this script:
  $ python get_oauth2_token.py
This is a combination of snippets from:
  https://developers.google.com/api-client-library/python/guide/aaa_oauth
  https://gist.github.com/burnash/6771295
'''

import os, sys
from oauth2client.client import OAuth2WebServerFlow
from oauth2client.tools import run_flow
from oauth2client.file import Storage


def return_token(): 
  return get_oauth2_token();


def disable_stout():
  o_stdout = sys.stdout 
  o_file = open(os.devnull, 'w')
  sys.stdout = o_file
  return (o_stdout, o_file)


def enable_stout(o_stdout, o_file):
  o_file.close()
  sys.stdout = o_stdout


def get_oauth2_token():
  CLIENT_ID = os.environ['CLIENT_ID']
  CLIENT_SECRET = os.environ['CLIENT_SECRET']
  SCOPE = 'https://www.googleapis.com/auth/spreadsheets'
  REDIRECT_URI = 'http://localhost:8080/'
 
  o_stdout, o_file = disable_stout()

  flow = OAuth2WebServerFlow(
   client_id=CLIENT_ID,
   client_secret=CLIENT_SECRET,
   scope=SCOPE,
   redirect_uri=REDIRECT_URI,
   access_type="offline")

  storage = Storage('creds.data')
  credentials = run_flow(flow, storage)
  enable_stout(o_stdout, o_file)

  print(f"export ACCESS_TOKEN='{credentials.access_token}'")

  if credentials.refresh_token == None:
    print("** Refresh token was empty. Try Deleting browser cache and try again")
  else:
    print(f"export REFRESH_TOKEN='{credentials.refresh_token}'")

if __name__ == '__main__':
  return_token()
