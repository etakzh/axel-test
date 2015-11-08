Task description:

We are a publishing company that created an app for reading news articles.

To serve data to the app we need a backend to provide RESTful APIs for the following use cases:

  - allow an editor to create/update/delete an article
  - display one article
  - list all articles for a given author
  - list all articles for a given period
  - find all articles for a specific keyword
  
Each API should only return the data that is really needed to fulfill the use case.
  
An article usually consists of the following information:

  - header
  - short description
  - text
  - publish date
  - author(s)
  - keyword(s)

Hints:

  - Use the Java technology you are most comfortable with (e.g. spring-boot).
  - The data doesn't need to be persisted after the application is shut down.

The main focus of implementing this task should be on quality of the code - so the code needs to be readable, maintainable and of course stable. In the best case the code is ready to be used in production.

Run:

1. java -jar axel-test.jar
2. Application consumes and produces JSON.