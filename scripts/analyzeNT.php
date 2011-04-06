#! /usr/bin/php
<?php 
ini_set('memory_limit', '5G');
$detailed = true;

/*
 * 
 * this script either has only one parameter giving one n-triple file:
 * ./script.php file.nt
 * or uses -d and a directory with nt files
 * ./script.php -d data
 */


$files=array();
if ($argv[1] == '-d') {
	$directory = $argv[2];
	if($directory[strlen($directory)-1]!= '/'){
		$directory.='/';
		}

		if ($handle = opendir($directory)) { 
			while (false !== ($file = readdir($handle))) { 
				if ($file != "." && $file != ".." && strpos($file, '.nt')!== false) {
					$files[] = $directory.$file; 
					
				}
		}} 
		closedir($handle);
}else{
	
	$files[] = 	$argv[1];
}




if(empty($files)){
	echo "no files found in $directory\n";
	die;
	}

echo "Files:\n";
foreach ($files as $file){echo $file."\n"; }
echo "\n\n";
foreach ($files as $file){
	$now = microtime(true);
	$literals = 0;
	$resourceobjects = 0;
	$triples = 0;
	$rdftype = 0;
	$owlproperty = 0;
	
	$uniquesubjects = array();
	$uniquepredicates = array();
	$uniqueobjects = array();
	$sameAslinks = array();
	$classes = array();
	
	$subsAreObjects = array();
	$objsAreSubjects = array();
	
	$handle = @fopen($file, "r") or exit("Unable to open file!");
	if ($handle) {
    while (!feof($handle)) {
        $line = fgets($handle);
		$triples+=1;
		if(strpos($line,'"')!==false){
			$literals+=1;
			continue;		
		}
		
		$res = splitit($line);
		
		if($res['o']=="<http://www.w3.org/1999/02/22-rdf-syntax-ns#Property>" ){
			continue;
		}
		
		
		if($res['o']=="<http://www.w3.org/2000/01/rdf-schema#Class>" ){
			add($classes, $res['s'] );
			continue;
		}
		if($res['p']=="<http://www.w3.org/2002/07/owl#sameAs>"){
			add($sameAslinks, $res['o'] );
			
			continue;
		}
		if($res['p']=="<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"){
			$rdftype +=1;
			add($classes, $res['o'] );
			continue;
		}
		if(strpos($res['p'], "<http://www.w3.org/2002/07/owl#") === 0){
			$owlproperty+=1;
			//echo $res['p'];die;
			//add($owlproperty, $res['p'] );
			continue;
		}
		
		
		$resourceobjects +=1;
		add($uniquesubjects, $res['s']);
		add($uniquepredicates, $res['p']);
		add($uniqueobjects, $res['o']);
		
	}//while
	}//if
    fclose($handle);
	
	$subsAreObjects = subsAreObjects( $uniquesubjects, $uniqueobjects);
	$objsAreSubjects = subsAreObjects( $uniqueobjects, $uniquesubjects);
	
	echo $file." needed: ".round((microtime(true)-$now),2)." sec \n";
	$table ["Literals (ignored)"] = $literals;
	$table ["sameAsLinks (ignored)"] = count($sameAslinks);
	$table ["rdf:type (ignored)"] = $rdftype;
	$table ["owlproperty (ignored)"] = $owlproperty;
	$table ["classes (ignored)"] = count($classes);
	$table ["Objects"] = $resourceobjects;
	$table ["Triples"] = $triples;
	$table ["Triples_with_Object_ratio"] = pretty($resourceobjects, $triples);
	$table ["unique_subjects"] = count($uniquesubjects);
	$table ["subsAreObjects"] = count($subsAreObjects);
	$table ["subsAreObjectsRatio"] = pretty(count($subsAreObjects),count($uniquesubjects));
	$table ["avg_indegree_subjects"] = avg($subsAreObjects);
	$table ["avg_outdegree_subjects"] = avg($uniquesubjects);
	$table ["unique_predicates"] = count($uniquepredicates);
	$table ["avg_usage_predicates"] = avg($uniquepredicates);
	
	$table ["unique_objects"] = count($uniqueobjects);
	$table ["objsAreSubjects"] = count($objsAreSubjects);
	$table ["objsAreSubjectsRatio"] = pretty(count($objsAreSubjects),count($uniqueobjects));
	$table ["avg_indegree_objects"] = avg($uniqueobjects);
	$table ["avg_outdegree_objects"] = avg($objsAreSubjects);
	p($table);
	
	if($detailed){
		echo "classes\n";
		detailed($classes);
		echo "predicates\n";
		detailed($uniquepredicates);
		}
	
}//foreach

function detailed ($arr){
		foreach ($arr as $key=>$value){
			echo "$key	$value\n";
			
			}
		echo "\n\n";
	}


function subsAreObjects($sub, $obj){
		$subsAreObjects = array();
		foreach($sub as $key=>$value){
				if(isset($obj[$key])){
					for ($x = 0; $x<$obj[$key];$x++){
						add($subsAreObjects, $key);
						}
					}		
			}
		return $subsAreObjects;
	}

function p($table){
	
		foreach ($table as $key=>$value){
			echo $value;
			echo "	";
			echo $key;
			echo "\n";
		}
		echo "\n\n";
	}

function add(&$arr, $key){
		if(!isset($arr[$key])){
			$arr[$key]=0;
			}
		$arr[$key]+=1;
	
	}

function avg($arr){
	    if(count($arr) == 0) {
			return 'NaN';
			}
		$avg = 0;
		foreach($arr as $key=>$value){
				$avg+=$value;
			}
		return round($avg/count($arr), 3);
	}


function splitit($rest){
		$res = array();
		$s = strpos($rest,'>')+1;
		$res['s'] = trim(substr($rest, 0, $s));
		$rest = trim(substr($rest, $s));
		$s = strpos($rest,'>')+1;
		$res['p'] = trim(substr($rest, 0, $s));
		$rest = trim(substr($rest, $s));
		$s = strpos($rest,'>')+1;
		$res['o'] = trim(substr($rest, 0, $s));
		//print_r($res);
		//die;
		return $res;
	}

function pretty($a, $b){
		if($b==0)return "NaN%";
		return round($a/$b*100,2)."%";
	}
