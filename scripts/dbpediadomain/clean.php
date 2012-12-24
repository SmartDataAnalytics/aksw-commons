#! /usr/bin/php
<?php 
ini_set('memory_limit', '5G');


$urifile =  $argv[1];
$datafile =	$argv[2];

$handle = @fopen($urifile, "r") or exit("Unable to open file!");
while(!feof($handle)){
    $line =  trim(fgets($handle));
    $uriIndex[$line] =1;
    }

fclose($handle);

$handle = @fopen($datafile, "r") or exit("Unable to open file!");

while(!feof($handle))
  {
  $line =  trim(fgets($handle));
  $pos = strpos($line, ">");
  $sub =  substr($line,0,$pos);
  $sub = trim(str_replace("<","",$sub));
  if(@$uriIndex[$sub]===1){
      echo $line."\n";
      }
  
  }
  
  fclose($handle);
    exit;
?>
