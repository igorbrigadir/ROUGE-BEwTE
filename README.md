BE: Basic Elements for Automated Evaluation of Summaries
======

Implementation for download: BEwT-E: Basic Elements with Transformations for Evaluation

Creators(s): Eduard Hovy, Chin-Yew Lin, Stephen Tratz, Liang Zhou (all from University of Southern California Information Sciences Institute), Junichi Fukumoto (Ritsumeikan University; visiting USC/ISI)

Contact: hovy@isi.edu

The Basic Elements (BE) method was developed to automatically evaluate text summaries.  This page describes the general BE model.  A current implementation of the model, BEwT-E (Basic Elements with Transformations for Evaluation) can be downloaded here (see below).

Background
======

It has long been a goal of the summarization community to find automatic methods of summary evaluation that produce reliable and stable scores.  Generally, summaries are evaluated along two dimensions: for content and for style (readability).

Basic Elements address only the problem of assessing the content of a summary.

All automated content assessment methods today work by comparing the input summary to one of more reference summaries (ideally, produced by humans).  But experience has shown that measuring summary content at the sentence level is not precise enough: generally sentences contain too many bits of information, some of which may be important to include in a summary while others may not be.

There have been two kinds of response to this problem: the word-sized and the chunk-sized.  In ROUGE (Lin at USC/ISI) and similar systems, the approach was to measure the overlap of each word (or small ngram) with the reference summaries.  The problem here is that multi-word units (such as "United States of America") are not treated as single items, thereby skewing the scoring, and that relatively unimportant words (such as "from") count the same as relatively more important ones.  Simple efforts to circumvent these problems remain unsatisfactory and crude.  Nonetheless, this approach can be automated and can produce evaluation rankings that correlate reasonably with human rankings, as demonstrated in the ROUGE publications.

The other response was to extract longer chunks, namely the strings of contiguous words that express valuable material, from one or more of the reference summaries, and to treat these chunks as pieces of ideal content.  Each chunk, regardless of length, is treated as a semantic unit, that is, a unit that expresses one core notion.  Each unit is assigned an importance rating depending on how many reference summaries contain it.  In recent research, Van Halteren and Teufel in Europe and Nenkova and Passonneau at Columbia University in New York have independently investigated this type of approach.  Since an element that is included in many reference summaries is obviously more important than one that is included in only a few, this method provides a natural way of scoring each element.  The latter two researchers create a 'pyramid' of elements, with the most-frequently-included ones at the top, the next-most one layer down, etc.  Evaluating a new summary then becomes a process of comparing its contents to the elements in the pyramid and adding the appropriate score for each one matched.   A higher score means the new summary overlapped with more of the reference summary contents and is hence assumed to be a better summary.  Preliminary studies show this approach to correlate well with human intuition.  The trouble is that creating these chunks is difficult to automate, since they can be of arbitrary size and must incorporate quite different ways of saying the same thing (reference summaries typically say the same thing, or parts of the same thing, in different ways).

Basic Elements
======

Basic Elements (BEs) were designed to address both problems by using variable-sized, syntactically coherent, units.  We start with small units, because starting small allows one to automate the process of unit identification and, to some degree, facilitates the matching different equivalent expressions.  Grouping smaller units into larger ones can be done automatically, and eventually, we believe, to the larger-sized chunks used in the Pyramid Method.

In this approach, we break down each reference sentence into minimal semantic units, which we call Basic Elements.  After some experimentation, we define BEs as follows:

A Basic Element is one of 1) the head noun of a major syntactic constituent (noun phrase or verb phrase).  In the current implementation, this includes: a noun (sequence) or a verb; 2) a relation (includes prepositions) between a head-BE and a single dependent 

As described below, one can produce BEs in several ways.  Most of them involve a syntactic parser to produce a parse tree and a set of 'cutting rules' to extract just the valid BEs from the tree.

With units of minimal length, one can much more easily decide whether any two units match (express the same meaning) or not.  For instance, "United Nations", "UN",and "UNO" can bematched at this level, and any larger unit encompassing this one can accept any of the three variants.  And since the units are matched at the lowest levels, the danger of potentially double-counting segments that are contained in longer ones can also be avoided.

To match non-identical units that carry the same meaning, we apply rules to transform each unit into a number of different variants.  The software downloadable here, BEwT-E (BEs with Transformations for Evaluation; pronounced "beauty"), is a package that automatically creates BEs for a text, applies transformation rules to expand BE units into numerous variants, and performs matching of these units against a list of units produced by BEwT-E from another text.

In order to implement Basic Elements as a method of evaluating summary content, four core questions must be addressed:

1. What or how large is a Basic Element? The answer to this is strongly conditioned by: How can BE units be created automatically?

2. What score should each BE unit have?

3. When do two BE units match?  What kinds of matches should be implemented, and how?

4. How should an overall summary score be derived from the individual matched BE units' scores?

Different answers to each of these questions provide a different summary evaluation method.  The Pyramid Method, for example, takes as BEs maximal-length semantic units shared by the reference summaries; gives each unit a score equal to the number of reference summaries containing it; allows two units to match when they express all or most of the same semantic content, as judged by the (human) assessors; and derives the overall score by simply summing the scores of each unit of the candidate summary.  In contrast, ROUGE uses as BEs various ngrams (for example, unigrams); scores each unigram by a function that depends on the number of reference summaries containing that unigram; allows unigrams to match under various parameterizable conditions (for example, exact match only, or root form match); and derives the overall summary score by some weighted combination function of unigram matches.

There are multiple possible approaches to implementing in software each of these four points; exploring the whole space in order to find the most stable and optimum evaluation configuration is obviously not a trivial task.  The current implementation of BEwT-E uses variable-length syntactically coherent units, gives each one the same score, matches units that are derivable from one another through the transformations, and weights and adds the match scores in various ways. 

BEs and ROUGE
======

ROUGE is a software package for automated summary evaluation that matches input summary to references summaries using a variety of fixed-length word ngrams.  ROUGE was built at USC/ISI by Lin and Hovy.  Note that ROUGE itself is also an instance of the BE framework, in which the BEs are unigrams (or ngrams of various types, depending on the parameter choice), the scoring function is simple unit points, and the simplest matching criterion is lexical identity.

BE and BEwT-E Software Packages
======

We created and distributed the Basic Element (BE) Package in 2005.  This package was a framework in which one can insert and/or vary modules that perform each of these four functions.  The BE Package provided several parameterized modules as well as APIs for people wishing to build and test their own.  Used as provided, the BE Package provided several implementations of the ideas of Van Halteren, Teufel, Nenkova, and Passonneau.  We performed a series of experiments to obtain reasonably good modules and parameter settings, but welcome additional studies and improvements.

We have created a new package BEwT-E (Basic Elements with Transformations for Evaluation) that includes transformations.  A current implementation of BEwT-E can be downloaded after filling in the form below and accepting the licensing terms.

Please direct all inquiries to hovy@isi.edu.

BE References
======

* Tratz, S. and E.H. Hovy. 2008. Summarization Evaluation Using Transformed Basic Elements. Proceedings of Text Analytics Conference (TAC-08).  NIST, Gaithersburg, MD.

* Zhou, L. N. Kwon, and E.H. Hovy. 2007. A Semi-Automated Evaluation Scheme: Automated Nuggetization for Manual Annotation. Proceedings of the Human Language Technology / North American Association of Computational Linguistics conference (HLT-NAACL 2007). Rochester, NY.

* Zhou, L. and E.H. Hovy. 2007. A Semi-Automatic Evaluation Scheme. Proceedings of the DARPA GALE PI workshop. San Francisco, CA.

* Hovy, E.H., C.-Y. Lin, L. Zhou, and J. Fukumoto. 2006.  Automated Summarization Evaluation with Basic Elements. Unpublished ms.
