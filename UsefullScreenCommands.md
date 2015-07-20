new session:
```
screen -S mySessionName
```

list sessions:
```
screen -ls
```

reattach to session:
```
screen -r mySessionName
```

reattach to last or only session:
```
screen -x
```

#### inside a screen ####
```
ctrl-a d  -- detach
ctrl-a c  -- create window
ctrl-a k  -- kill window
ctrl-a n  -- next window
ctrl-a p  -- previous window
```