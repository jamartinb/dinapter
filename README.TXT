Dinapter v1.2
=============

Authors
-------

 * José Antonio Martín Baena <jose.antonio.martin.baena@gmail.com>
 * Ernesto Pimentel Sánchez <ernesto@lcc.uma.es>

Introduction
------------

Nowadays, application design using off-the-shelf software components has several advantages like greater productivity and software reusability. But this design based on black-boxed components has to face an issue: the adaptation of components with mismatches in signature and behavior levels. Currently there're efforts towards signature mismatches such as Interface Description Languages (IDLs) but these techniques fail to address behavioural incompatibilities. This paper is focused in the adaptation of both signature and behaviour inconsistencies.

Brogy, Canal and Pimentel developed a formal methodology aimed to automatically derive an adaptor knowing the interfaces and behaviours of the components. This methodology is based on the initial agreement between the parts involved about the adaptor specification. This specification contains a map between the methods of the components in such a way that, when the adaptor applies these correspondences in a proper sequence, all the components cooperate properly and they finally end in a consistent state. Brogy, Canal and Pimentel also proposed an algorithm to derive the adaptor once the mappings between the messages were known but these mappings were supposed to be introduced at the beginning. In this work we introduce an implementation approach which finally addresses this issue.

The implementation proposed intend to make up the specification building the mappings incrementally. Step by step it traces the behavior of the components adding the messages found to the mappings in several ways, it values the partial specifications and continues working on the most promising ones until it finds the final specification. Currently a combination of A* and expert systems is used to achieve this functionality.

Folders
-------

[Some of them might been removed due to space restriction]

    src                     - Source code.
    src/rules               - Expert system rules.
    doc                     - Documentation.
    doc/api                 - Javadoc.
    doc/article/article.pdf - Article submitted to SYANCO about Dinapter.
    doc/paper/paper.pdf     - Detailed documentation (Spanish).
    dist                    - Deployed version with everything needed to run.
    licenses                - Licenses of the used libraries.
    lib                     - Libraries used.

Ant tasks
---------

    get-jess - Downloads and installs locally  a fresh trial-verion of Jess.
    build    - Compiles the project.
    javadoc  - Generates the API files.
    dist     - Deploys the project.

Usage
-----

    > ant get-jess # Gets a fresh copy of Jess (it's a trial version).
    > ant dist # Redeploys Dinapter if needed.
    > cd dist  # Get into the deployment dir.
    > # Execute Dinapter with the behavior of two simple FTP components.
    > java -jar Dinapter.jar input/e002c-ftp_small/client.bpelj input/e002c-ftp_small/server.bpelj -v

    Other ways to execute Dinapter are:
         * Just giving the folder which contains two bpel files:
            > java -jar Dinapter.jar input/e002c-ftp_small -v
         * Using the given shortcut script:
            > ./dinapter input/e002c-ftp_small -v

    However, the python script requires JPype and some configurations, so the java execution is the preferred alternative.

    The "-v" argument just tells Dinapter to display the behaviors of the given processes.


    It's possible to change some inner parameters of dinapter. These paramenters, and the way to use them, are explained in "dist/etc/README.TXT"

Used libraries
--------------

    jess        - http://herzberg.ca.sandia.gov/jess/
    jsearchdemo - http://el-tramo.be/software/jsearchdemo/
    jpowergraph - http://sourceforge.net/projects/jpowergraph/
    junit       - http://sourceforge.net/projects/junit/
    log4j       - http://logging.apache.org/log4j

A word about Jess
-----------------

    The included Jess version is a trial one so it might be expired. If so please go to http://www.jessrules.com/jess/download.shtml and download it again (the trial is for free). The version of Jess used is the 6.1p8 but it should work with any other above. Once downloaded, replace dinapter's "lib/jess.jar" by the file "lib/jess.jar" inside the downloaded zip file.

License
-------

    Dinapter is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    Dinapter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    (C) Copyright 2007 José Antonio Martín Baena
    
    José Antonio Martín Baena <jose.antonio.martin.baena@gmail.com>
    Ernesto Pimentel Sánchez <ernesto@lcc.uma.es>
