#useful spring stuff
# Resource management #
You want to load your quirky resources/whatever file, like a configuration for some legacy component? Always trouble? Lets assuzme we want to load the file "myconf.xml" so that our class "Myclass" can do stuff with it. We use spring resources! just add a field in your bean on type Resource (spring/core/io), like in here:

```
class Myclass{

  private Resource myconf;

  public void setMyConf(Resource myconf){
    this.myconf = myconf;
  }
}
```

than add in your config file to the target bean

```
<bean name="smurf1" class="Myclass">
  <property name="myconf" value="classpath:myconf.xml"/>
</bean>
```

The classpath resource "myconf.xml" is injected. Magic. Why did this take so long for me to find out?