### Adding a new key to a server ###
```
cd /root/.ssh
cd public_keys
echo "sfdjklgwepoeofmks" > WernerMuelle.pub
cat * > ../authorized_keys
```


### Saving default host on the local machine ###
Add this to ~/.ssh/config (use what you want for short)
```
Host short
	Hostname our.server.org
	User root
```
Log in to the server with:
```
ssh short
```