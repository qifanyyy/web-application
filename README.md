# cs122b-spring20-team-12

cs122b-spring20-team-12 created by GitHub Classroom

## Create Database

```bash
mysql -umytestuser -p < createtable.sql
mysql -umytestuser -p moviedb < movie-data.sql
mysql -umytestuser -p moviedb < add-quantity-to-sales.sql
```

## Server

See [server/README.md](./server/README.md).

## XML Parser

See [xml_parsing/README.md](./xml_parsing/README.md).

## Project 3

### Project 3 Team Contribution

Qifan Yu
- register a domain for fabflix
- add https, use prepared statement
- use encrypted password
- implement a dashboard using stored  procedure

Tongjie Wang
- add reCAPTCHA
- add prepare statement
- import large XML data files into the Fabflix database

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
