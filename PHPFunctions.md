### Starts With ###
```
function startsWith($Haystack, $Needle){
    // Recommended version, using strpos
    return strpos($Haystack, $Needle) === 0;
}
```
### Ends With ###
```
function endsWith($Haystack, $Needle){
    // Recommended version, using strrpos
    return strrpos($Haystack, $Needle) === (strlen($Haystack)-strlen($Needle));
}
```
## Removes e.g. "quotes" ##
```
function stripBeginningOrEnd($object, $what){
        if(startsWith($object, $what)) {
            $object = substr($object,0);
            }
        if(endsWith($object,$what)){
            $object = substr($object,1,-1);
        }
        return $object;
    }
```