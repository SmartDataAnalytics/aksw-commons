

#### Virtuoso HTTP Log ####
HTTPLogFile    = log.out


#### adjust memory usage in virtuoso.ini ####
```
; see http://docs.openlinksw.com/virtuoso/dbadm.html#ini_Parameters
; a total of 65% of available memory should be given to virtuoso
; MaxDirtyBuffers = 75% * NumberOfBuffers
; Examples:***************************************
; 65% of 8 Gb is 5.2 GB
; NumberOfBuffers = 650000
; MaxDirtyBuffers = 487500
;******************************
; 4GB / 8 = 500000 Buffers
; dirty = 75% of 500000 = 375000
; NumberOfBuffers = 500000
; MaxDirtyBuffers = 375000
;******************************
; 3GB / 8 = 333333 Buffers
; dirty = 75% of 250000 = 250000
; NumberOfBuffers = 333333
; MaxDirtyBuffers = 250000
;******************************
; 2GB / 8 = 250000 Buffers
; dirty = 75% of 250000 = 187500
; NumberOfBuffers = 250000
; MaxDirtyBuffers = 187500
;******************************
NumberOfBuffers            = 333333
MaxDirtyBuffers            = 250000
```