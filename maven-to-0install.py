#!/usr/bin/env python

def make_feed(maven_repository, artifact_group, artifact_id, feed_file):
	run_xslt(
		input_uri = maven_repository + artifact_group + '/' + artifact_id,
		output_file = feed_file,
		maven_repository = maven_repository)
	add_digests(
		feed_file = feed_file,
		maven_repository = maven_repository)

def run_xslt(input_uri, output_file, maven_repository):
	pass

def add_digests(feed_file, maven_repository):
	import xml.etree.ElementTree as xml
	feed = xml.parse(zip.open(feed_file))
	root = feed.getroot()

if __name__ == "__main__":
	import sys
	make_feed(
		maven_repository = sys.argv[1],
		artifact_group = sys.argv[2],
		artifact_id = sys.argv[3],
		feed_file = sys.argv[4])
