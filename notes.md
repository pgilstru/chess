# Notes

Here are my notes :)

## GitHub Commands

1. Stage all modified files
   ```git add .```
2. Commit changes
   ```git commit -m "message here"```
3. Push changes
   ```git push```



## Chess UML Sequence Diagram

![chessSequenceDiagram](ChessSequenceDiagram2.png)



## Phase 3 Notes

Inside of server/src/main/java/Main.java in the main method, create a Server object, and then call run on it. The run method needs the port you will run your server on, which typically for testing is 8080. When you run the main method it will start the server. Intelij will give you several lines of red text, but if the last line says 'started' then the server is active.

### Implementation order
1. [x] Set up your starter code so that your server runs properly, and make sure the testing webpage loads.
2. [x] Use your sequence diagrams and the class diagram at the top of this page to guide the decision for what classes you might need.
   - Data Model Classes:
     - UserData
     - GameData
     - AuthData
   - Data Access Classes:
     - UserDAO
       - Methods: clear(), getUser(username), createUser(userData)
     - GameDAO
       - Methods: clear(), getGame(gameID), createGame(gameData), listGames(), updateGame(gameData)
     - AuthDAO
       - Methods: clear(), getAuth(authToken), createAuth(authData), deleteAuth(authToken)
   - Service Classes:
     - ClearService
       - Covers: clear()
     - GameService
       - Covers: create(), join(), list()
     - UserService
       - Covers: register(), login(), logout()
3. [ ] Create packages for where these classes will go, if you haven't already done so.
4. [ ] Pick one Web API endpoint and get it working end-to-end. We recommend starting with clear or register.
   1. [ ] Create the classes you need to implement the endpoint.
   1. [ ] Write a service test or two to make sure the service and data access parts of your code are working as you expect.
   1. [ ] Make sure you can hit your endpoint from the test page on a browser or Curl. Verify the response is what you expect it to be.
5. [ ] Repeat this process for all other endpoints.


Data Model Classes:

- UserData
- GameData
- AuthData

! ended at request and result classes!


# References: 

## Phase 3:

https://www.uuidgenerator.net/dev-corner/java

https://www.w3schools.com/java/java_hashmap.asp
