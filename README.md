# cs122b-spring20-team-12

cs122b-spring20-team-12 created by GitHub Classroom

## Create Database

```bash
mysql -umytestuser -p < createtable.sql
mysql -umytestuser -p moviedb < movie-data.sql
mysql -umytestuser -p moviedb < add-quantity-to-sales.sql
mysql -umytestuser -p moviedb < set-missing-rating-to-zero.sql
mysql -umytestuser -p moviedb < stored-procedure.sql
```

## Server

See [server/README.md](./server/README.md).

## XML Parser

See [xml_parsing/README.md](./xml_parsing/README.md).

## Password Encryption

See [encrypt_password/README.md](./encrypt_password/README.md)

## Project 3

### Demo

- https://www.ics.uci.edu/~tongjiew/cs122b_demo3.mov (backup, UCI ICS)
- https://drive.google.com/open?id=1xz8mGC2ft5IaRERoabmVkIZx_HFCTNtj (backup, Google Drive, need UCI account)

### XML Parsing Report

With our first implementation, the parsing and insert took more than 8 minutes to complete (no exact time available since we just terminated our program and started working on improvement).

Later, methods such as using a hash set to remove duplicates from xml files and adding indexes to speed up database-level duplication detection were added and the program tooks about 1 minute and 30 seconds to finish.

Finally, methods such as multithreading and batch insert were added and the program took about 1 minute to finish.

See [this issue](https://github.com/UCI-Chenli-teaching/cs122b-spring20-team-12/issues/2) or [Google Drive link](https://drive.google.com/open?id=1DpzkE_DnSuwqXifNqH6XxaKOgUlZ8NRz) (need UCI account) for inconsistency report.

### Queries with Prepared Statements

Files that use prepare statement:
- MoviesServlet
- SingleMovieServlet
- SingleStarServlet

Link to the files on GitHub:
- MoviesServlet:
- https://github.com/UCI-Chenli-teaching/cs122b-spring20-team-12/blob/master/server/src/MoviesServlet.java
- SingleMovieServlet:
- https://github.com/UCI-Chenli-teaching/cs122b-spring20-team-12/blob/master/server/src/SingleMovieServlet.java
- SingleStarServlet:
- https://github.com/UCI-Chenli-teaching/cs122b-spring20-team-12/blob/master/server/src/SingleStarServlet.java

### Project 3 Team Contribution

Qifan Yu
- Register a domain for fabflix
- Add HTTPS
- Use prepared statement
- Encrypt password for `customers` table
- Implement `add_movie` stored procedure

Tongjie Wang
- Add reCAPTCHA
- Encrypt password for `employees` table
- Implement a dashboard using stored procedure
- Import large XML data files into the Fabflix database

## Project 2

- https://youtu.be/eZkls9E3f-k
- https://drive.google.com/open?id=1faFV4_jPZK5V6kzxVqDxA706JQZZhLj4 (backup, need UCI Gmail account)
- https://youtu.be/_RVvliXncns
- We include 2 Youtube links in the Readme because in the first demo, we accidentally choose the wrong order <acse, acse> for the love search result, so we add second demo link to display the result.

Substring matching design:

We use LIKE in 18 mysql query of MoviesServlet.java from line 46 to 63.
- In line 46, I use movies.title LIKE "alnum%" to include the movies that starts with single alphanumerical (0,1,2,3..A,B,C...X,Y,Z) characters
- In line 47, I use name LIKE '"+genre+"'" to search for the movies that belong to certain genres
- In line 49, when (title is empty, year is empty, stat is empty, director is empty), I use movies.title LIKE '%"+title+"%'" to search for the movies that include certain characters in the title
- In line 50, when (!t && !y && !s && !d), I use movies.year = '"+year+"'" to search movies that their year include certain characters
- In line 51, when ( t && !y && !s && !d), I use movies.title LIKE '%"+title+"%'" to search for movies that include certain characters in title
- In line 52, when (!t && !y &&  s && !d), I use name LIKE '%"+star+"%'" to search for movies that include certain characters in the star name
- In line 53, when ( t &&  y && !s && !d), I use movies.year = '"+year and movies.title LIKE '%"+title+"%'" to search for movies that include certain characters in title and year
- In line 54, when ( t && !y && !s &&  d), I use movies.director LIKE '%"+director+"%' AND movies.title LIKE '%"+title+"%'" to search for movies that include certain characters in title and director
- In line 55, when ( t && !y &&  s && !d), I use movies.title LIKE '%"+title+"%' AND name LIKE '%"+star+"%'" to search for movies that include certain characters in title and star
- In line 56, when (!t &&  y && !s &&  d), I use movies.year = '"+year+"' AND movies.director LIKE '%"+director+"%'" to search for movies that include certain characters in year and director
- In line 57, when (!t &&  y &&  s && !d), I use movies.year = '"+year+"%' AND name LIKE '%"+star+"%'" to search for movies that include certain characters in year and star
- In line 58, when (!t && !y &&  s &&  d), I use movies.director LIKE '%"+director+"%' AND name LIKE '%"+star+"%'" to search for movies that include certain characters in director and star
- In line 59, when ( t && !y &&  s &&  d), I use movies.title LIKE '%"+title+"%' AND movies.director LIKE '%"+director+"%' AND name LIKE '%"+star+"%'" to search for movies that include certain characters in director, star and title
- In line 60, when (!t &&  y &&  s &&  d), I use movies.year = '"+year+"%' AND movies.director LIKE '%"+director+"%' AND name LIKE '%"+star+"%'" to search for movies that include certain characters in director, star and year
- In line 61, when ( t &&  y &&  s && !d), I use movies.year = '"+year+"%' AND movies.title LIKE '%"+title+"%' AND name LIKE '%"+star+"%'" to search for movies that include certain characters in title, star and year
- In line 62, when ( t &&  y && !s &&  d), I use movies.year = '"+year+"' AND movies.director LIKE '%"+director+"%' AND movies.title LIKE '%"+title+"%'" to search for movies that include certain characters in title, director and year
- In line 63, when ( t &&  y &&  s &&  d), I use movies.director LIKE '%"+director+"%' AND movies.year = '"+year+"%' AND movies.title LIKE '%"+title+"%' AND name LIKE '%"+star+"%'" to search for movies that include certain characters in title, star, director and year

### Project 2 Team contribution

Qifan Yu
- Main page browse and search 
- Movie List page, jump function and Single Pages 

Tongjie Wang
- Login page
- Shopping cart page, Payment Page, Place Order Action

## Project 1

Each of the member contributes approximately same amount of work to the repository.

- https://youtu.be/gOmATkxsDes
- https://drive.google.com/file/d/18nEbfqveqkul2HjDjRrNMgDTht2SCMOQ/view?usp=sharing (backup, need UCI Gmail account)
