ff-fedora-client
================

A simple Fedora 4 client based on Apache's [HttpComponents](http://hc.apache.org/)

Example
-------

	FedoraClient client = new FedoraClient(URI.create("http://localhost:8080/fedora"));
	client.getObjectProfile("object:1");
