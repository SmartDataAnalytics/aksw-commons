Stuff needed for xdebug the profiler and eclipse debugging
```
[Xdebug]
xdebug.default_enable=On
xdebug.overload_var_dump=On
xdebug.var_display_max_children=256
xdebug.var_display_max_data=4096
xdebug.var_display_max_depth=8

xdebug.remote_enable=On
xdebug.remote_autostart=Off
xdebug.remote_mode="req"
xdebug.idekey="eclipse"

xdebug.profiler_enable=0
xdebug.profiler_output_dir="/tmp"
xdebug.profiler_output_name="xdebug.profiling-%R"
```


MediaWiki turn off the cache
```
$wgMainCacheType = CACHE_NONE;
$wgMessageCacheType = CACHE_NONE;
$wgParserCacheType = CACHE_NONE;
$wgCachePages = CACHE_NONE;
```