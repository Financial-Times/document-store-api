#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import requests
import sys
import re, ast, json, pprint, os
from time import gmtime, strftime

'''
Example json document:
{
   "items" : [ 
       {
           "uuid" : "8405dc04-40a0-11e5-9abe-5b335da3a90e"
       }, 
       {
           "uuid" : "f102aa60-40c5-11e5-9abe-5b335da3a90e"
       }, 
       {
           "uuid" : "03423eac-4085-11e5-b98b-87c7270955cf"
       }, 
       {
           "uuid" : "ba711b68-40dd-11e5-9abe-5b335da3a90e"
       }, 
       {
           "uuid" : "1052c05a-40bf-11e5-9abe-5b335da3a90e"
       }, 
       {
           "uuid" : "b160b7f4-bdfb-11e5-9c55-e0da0d0e5dcf"
       }, 
       {
           "uuid" : "a2df3816-bf83-11e5-9c55-e0da0d0e5dcf"
       }, 
       {
           "uuid" : "e500b846-40cb-11e5-9abe-5b335da3a90e"
       }, 
       {
           "uuid" : "5210a6e2-4074-11e5-9abe-5b335da3a90e"
       }
   ],
   "title" : "UK Top Stories",
   "uuid" : "520ddb76-e43d-11e4-9e89-00144feab7de",
   "layoutHint" : "standard",
   "publishReference" : "tid_dj1z4nj1i0",
   "lastModified" : "2016-01-20T17:38:46.738Z"
}

'''

class listPut:
    def __init__(self, args):
        self.ok = '\033[94m'
        self.endc = '\033[0m'
        self.fail = '\033[91m'
    
    def generateLastModified(self):
        # Returns UTC  timestamp in format of 2016-06-21T07:41:49.000Z 
        return strftime("%Y-%m-%dT%H:%M:%S.000Z", gmtime())
    
    def loopValuesCsv(self, key, values):
        listOfValues = values.split(",")
        doc = ''
        remaining = len(listOfValues)
        for each in listOfValues:
            doc = doc + self.outputString('{ ')
            doc = doc + self.outputString('"' + key + '"' + ' : ' + '"' + each.strip() + '"')
            doc = doc + self.outputString(' }')
            remaining = remaining - 1
            if  remaining > 0:
                doc = doc + self.outputString(',')
        return doc
    
    def outputString(self, string, indentation = 0):
        # Add indentation + string and return it
        i=0
        output = ''
        while i < indentation:
            output = output + '\t'
            i=i+1
        output = output + str(string)
        return output
    
    def print_ok(self, input):
        print self.ok + input + self.endc
    
    def print_fail(self, input):
        print self.fail + input + self.endc    
    
    def run(self):

        jsondoc = self.outputString('{',)
        jsondoc = jsondoc + self.outputString('"items" : [')
        listOfContent = self.loopValuesCsv('uuid', str(args.c))
        jsondoc = jsondoc + self.outputString(listOfContent)
        jsondoc = jsondoc + self.outputString('],')
        jsondoc = jsondoc + self.outputString('"lastModified" : "' + self.generateLastModified() + '",')
        jsondoc = jsondoc + self.outputString('"title" : "' + args.t + '",')
        jsondoc = jsondoc + self.outputString('"uuid" : "' + args.i + '",')
        jsondoc = jsondoc + self.outputString('"layoutHint" : "' + args.l + '",')
        jsondoc = jsondoc + self.outputString('"publishReference" : "' + args.x + '"')
        jsondoc = jsondoc + self.outputString('}')
        
        # VALIDATE JSON DOCUMENT       
        try:
            jsonobj = json.loads(jsondoc)
        except Exception, e:
            self.print_fail("Wanky Wankerson couldn't load JSON document. Invalid input data: " + str(e))
            sys.exit(1)
        # PRINT JSON DOCUMENT
        self.print_ok(jsondoc)

             
parser = argparse.ArgumentParser(description='Document Store API - List PUT json document generator')
parser.add_argument('-c', help='Comma-separated list of content UUIDs', required=True)
parser.add_argument('-i', help='List UUID', required=True)
parser.add_argument('-l', help='Layout hint, e.g. standard', required=True)
parser.add_argument('-t', help='Title, e.g. UK Top Stories', required=True)
parser.add_argument('-x', help='Transaction ID, e.g. tid_dj1z4nj1i0', required=True)

args = parser.parse_args()

client = listPut(args)
sys.exit(client.run())