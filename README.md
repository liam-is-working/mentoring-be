# mentoring-be

## To run this project locally:
### Prerequisite:
1. Docker installed

### Steps:
1. Navigate your terminal to project root folder
2. Run: ```docker-compose up -d ``` to start project  
  
  - **Web server** is hosted at: http://localhost:8080/mentoring  
  
  - **Postgres database** is hosted at: jdbc:postgresql://localhost:5432      
    - **database name**: postgres  
    - **database user**: liam  
    - **database password**: 123   
3. Shutdown and clear: ```docker-compose down ```  
 - After ```docker-compose down ```  data in DB will be cleared!!!

## Notes: 
1. API swagger location: ./mentoring.yaml from root directory.
2. Please clear firebase data after testing.
