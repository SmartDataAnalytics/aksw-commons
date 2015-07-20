
```
alias -s tex="rubber --inplace --maxerr -1 --short --force --warn all --pdf"
```

# Top ten latex advice #

### 1. emphasize text ###
Never underscore text! It is a relic of typewriters. Use _italic_ text instead for emphasis and for proper names.
```
I quit smoking \emph{20 times} already. The \emph{Charles Bridge} is a big tourist attraction.
```

### 2. number and unit spacing ###
Long numbers like 100000 are more easy to read with spaces: 100 000. However, a whole space is too big for a number. The same problem occurs with units: 500km is too short, 500 km is too wide as units like km should be more tightly placed than "500 animals". The solution is using the package sepnum for numbers and create macros for numbers and units using a half-space "\," instead of the normal ones.

```
\usepackage{sepnum}
\begin{document}
```
English:
```
% if you want spaces for fractional digits place a "\," in the second {} too
\newcommand{\val}[1]{\sepnum{.}{\,}{}{#1}}
\newcommand{\valunit}[2]{\val{#1}\,#2}
\newcommand{\valrange}[2]{\val{#1} -- \val{#2}}
\newcommand{\valunitrange}[3]{\val{#1} -- \valunit{#2}{#3}}
```
German:
```
% if you want spaces for fractional digits place a "\," in the second {} too
\newcommand{\val}[1]{\sepnum{,}{\,}{}{#1}}
.. under construction...
```

Use full stops "." for numbers even in german language as it's neccessary for the sepnum package!

\val{100000} is a big number. Only \valunit{0.000012}{\%} of our products are defective.

### use a template ###
```
\input{template_english}
```
Don't write the .tex extension, using a template for each language is highly recommended if you use latex often.

### use microtype ###
You don't have to do anything to use microtype, just do \usepackage{microtype} in your preamble and your text magically looks much nicer!

Explanation: Normal text is justified ("Blocksatz") in latex. This means that the left and right borders of all lines of text have the same x - Coordinate. As different letters have different degrees of blackness (e.g "G" vs ".", the right line may look jagged. Microtype now creates lines of different width that look more like if they would have the same width than if they really have the same width :-)

### UTF8 input encoding ###
```
\usepackage[utf8]{inputenc}
```

### clickable links ###
```
\usepackage{url}
\usepackage[pdfborder={0 0 0}]{hyperref}
...
\begin{document}
\url{http://www.test.de}
```
Hyperref only works with pdflatex, not with dvips.
If you want the links highlighted, leave out the pdfborder= ... part.


### enable copying of text from the resulting PDF ###
```
\usepackage[T1]{fontenc}
\usepackage{lmodern} % provoke vector font
```

### use colors for text ###
```
\usepackage{color}
\begin{document}
\begin{color}{red}Recipe for extremely hot chilly sauce!\end{color}
```

### modern citation style with name and year ###
```
\usepackage[comma,authoryear]{natbib}
\begin{document}
\bibliographystyle{natdin}	% if you use german language. Formats bibliography according to DIN 1505. You may have to download and install natdin manually
\bibliographystyle{plainnat}	% if you use english language
...
The problem is equal to \emph{Maximum Coverage} \citep[p. 25, Problem 2.18]{approximation_algorithms}. See also \citet[p. 26 -- 27]{approximation_algorithms}.
...
\bibliography{mybibliography}	% without .bib extension
```
Output:
The problem is equal to Maximum Coverage (Vazirani, 2004, p. 25, Problem 2.18) with unit weights. See also Vazirani (2004, p. 26 - 27).

see also http://merkel.zoneo.net/Latex/natbib.php
see also http://www.tex.ac.uk/tex-archive/biblio/bibtex/contrib/german/din1505/natdin.bst

### use german language ###
```
\usepackage[ngerman]{babel}
\selectlanguage{ngerman}
```

# tables #

### tabular with pagebreak ###
```
\usepackage{longtable}
\begin{document}
\begin{longtable}[ll]
99 bottles of beer on the wall	&99 bottles of beer.\\
...
1 bottle of beer on the wall	&1 bottle of beer.\\
\end{longtable}
```

### tabular with line breaks for text alignments (flush left,flush right, justified = "Blocksatz" in german, centered) ###
```
\usepackage{tabulary}
\begin{document}
\tymin=20pt % optional, minimum row width
\tymax=210pt % optional, maximum row width
\begin{tabulary}{\textwidth}{LCRJ}
left left	&center center	&right right	&justified justified\\
\end{tabulary}
```

### professional tables ###
```
\usepackage{booktabs}
\begin{document}
\begin{tabular}{lr}
\toprule
person	&salary\\
\midrule
nick	&100\\
rick	&200\\
hick	&300\\
\bottomrule
\end{tabular}
```
Don't use vertical rules in tables, they are out of style!

###  ###
```

```
###  ###
```

```
###  ###
```

```
###  ###
```

```