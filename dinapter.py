#!/usr/bin/python
# coding=utf-8
"""\
This module serves as python interface to Dinapter. It requires JPype 
(http://jpype.sourceforge.net/). It translates from STS to Dinapter internal model,
it calls Dinapter and finally returns the generated contracts written in INCA default model.
STS Interfaces-> Dinapter -> Contracts

Name:   dinapter.py - Integration of Dinapter within Itaca.
Author: José Antonio Martín Baena
Date:   20-08-2008
"""

##
# This file is part of ITACA-Dinapter.
#
# ITACA-Adaptor is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# ITACA-Adaptor is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with ITACA-Adaptor.  If not, see <http://www.gnu.org/licenses/>.
##

# Required
from jpype import *;
from copy import deepcopy;
import os;
import logging;
from itacalib.model.contract import *;
from itacalib.model.interface import Interface;
from itacalib.model.lts import *;
from time import time;

from itacalib.Sim import sim;

# Optional
import itacalib.XML.stsxmlinterface as xml2sts;
import itacalib.XML.bpelparser as bpelparser;
from itacalib.XML.stsxml import writeXML;
import sys, getopt, xml;
from urllib import urlopen; # For WordNet integration.
from time import sleep;

# Load default logging configuration.
logging.basicConfig(level=logging.DEBUG);
#logging.basicConfig(level=logging.INFO);
#logging.basicConfig();

## Logger for this module
log = logging.getLogger('dinapterpy')

## Path to the JVM library. Modify if needed.
JVM_LIB = "/usr/lib/jvm/java-6-sun-1.6.0.13/jre/lib/amd64/server/libjvm.so";

## Path to the DinapterPy directory.
DINAPTERPY_PATH = "/home/arkangel/workspace/itaca/tools/Dinapter";

## It disables the use of SimTool to get compatibility measures between the operations.
DISABLE_SIM = True;

## If DISABLE_SIM = True, this uses an alternative WordNet web-interface to get similarities between the operations.
USE_WORDNET = False;

## It enables Wordnet in SimTool.
SIM_WORDNET = False;

## inTransition - SimTool parameter
SIM_INTRANSITION = True;

##
# If True, DinapterPy will generate complex vLTS, otherwise it will generate trivial
# vLTS where all the transitions loop on a single state, which is initial and final/stable
# at the same time.
GENERATE_VLTS = False;

##
# When a service has more than this transitions, unmatched arguments will be removed from
# its behavior in order to alleviate Dinapter's heuristic burden.
UNMATCHED_TRANSITIONS_TREASHOLD = 12;

##
# If DinapterTool takes more than these seconds, DinapterTool will be interrupted and reset, 
# ant it will return no contract. 
# If it's <= 0, DinpaterTool will never be interrupted. 
# This is the default value but it can be overriden.
DEFAULT_TIMEOUT = long(80); # 1:20 (mm:ss)

## Whether or not Dinapter class has already been loaded.
dinapterLoaded = False;

## Dinapter class, if loaded.
Dinapter = None;

_removedPlaceholders = {};

##
# dinapter.py
#
# Executes the Dinapter automatic contract generation tool with the given two interfaces
# written in stsxml and creates several contracts in "c_*.xml" files.
#
# USAGE: dinapter.py [ -t | -h | <1st_stsxml_interface_file> <2nd_stsxml_interface_file> ]
def main():
    try:
        opts, args = getopt.getopt(sys.argv[1:], "ht", ["help","test"]);
    except getopt.error, msg:
        log.fatal(msg);
        log.fatal("for help use --help");
        sys.exit(4);
    for o, a in opts:
        if o in ("-h", "--help"):
            log.info("""
Executes the Dinapter automatic contract generation tool with the given two interfaces written in stsxml and creates several contracts in "c_*.xml" files.

USAGE: dinapter.py [ -t | -h | <1st_stsxml_interface_file> <2nd_stsxml_interface_file> ]""");
            sys.exit(0);
        if o in ("-t", "--test"):
            log.info("Starting test, this could take a couple of minutes...");
            sys.exit(test());
    if len(args) != 2:
        log.fatal("This command needs two arguments: two interfaces in stsxml format.");
        sys.exit(2);
    for path in args:
        if not os.path.exists(path):
            log.fatal("One of the given paths doesn't exist: %s" % path);
            sys.exit(3);
    interfaces = [];
    for file in args:
        try:
            interfaces.append(xml2sts.readXML(file));
        except xml.parsers.expat.ExpatError, message:
            log.fatal('One of the given files ("%s") could not be parsed: \n\t%s' % (file,message));
            sys.exit(5);
    dinapterpy = DinapterPy();
    contracts = dinapterpy.generateContracts(interfaces[0],interfaces[1]);
    __writeContracts(contracts);
    end = raw_input('Do you want to generate more contracts? (y/N)');
    while str(end).lower() == "y":
        contracts = dinapterpy.generateMoreContracts();
        __writeContracts(contracts);
        end = raw_input('Do you want to generate more contracts? (y/N)');
    log.info(" -- The End --");

def __removeOldContracts():
    for file in os.listdir('.'):
        if (file.find('c_') != -1) and \
                file.endswith('.xml'):
            log.debug('Removing %s.' % file);
            os.remove(file);

def __writeContracts(contracts):
    __removeOldContracts();
    for contract in contracts:
        writeXML(contract.getName()+".xml", contract);
    log.info('The contracts were created as "c_*.xml"');
 
##
# Tests the equality among several contracts and its parts.
#
def testContractEq():
    ve1 = VectorElement("queso", "IN", ["hola","mundo"]);
    ve2 = VectorElement("queso", "IN", ["hola","mundo"]);
    ve3 = VectorElement("cosa","OUT");
    ve4 = VectorElement("cosa","OUT");
    if ve1.getType() != ve2.getType():
        raise Exception, "Vector element types are not the same.";
    if ve1.getName() != ve2.getName():
        raise Exception, "Vector element names are not the same.";
    if ve1.getData() != ve2.getData():
        raise Exception, "Vector element data are not the same.";
    if ve1 != ve2:
        raise Exception, "Vector elements are not equivalent.";
    if ve1 == ve3:
        raise Exception, "Different vector elements are equivalent.";
    v1 = Vector("v1");
    v1.addElement(ve1, "c");
    v1.addElement(ve3, "s");
    v2 = Vector("v2");
    v2.addElement(ve4, "s");
    v2.addElement(ve2, "c");
    if v1 != v2:
        raise Exception, "Vectors are not equivalent.";
    v3 = Vector("v3");
    v3.addElement(ve3, "s");
    v4 = Vector("v4");
    v4.addElement(ve4, "s");
    if v1 == v3:
        raise Exception, "Different vectors are equivalent.";
    c1 = Contract("c1");
    c1.addVector(v1);
    c1.addVector(v3);
    c2 = Contract("c2");
    c2.addVector(v4);
    if c1 == c2:
        raise Exception, "Different contracts are equivalent.";
    c2.addVector(v2);
    if c1 != c2:
        raise Exception, "Contracts are not equivalent.";
    #for element in [ve1, ve2, ve3, ve4, v1, v2, v3, v4, c1, c2]:
    #    print hash(element);

##
# It tests the sts2dinapter translator, Dinapter integration and later dinapter2contract translator.
#
def testVectorFilter():
    filepath = os.path.join(os.path.abspath('.'), 'input/e018d-SAC09/client.xml');
    interfaceA = xml2sts.readXML(filepath);
    filepath = os.path.join(os.path.abspath('.'), 'input/e018d-SAC09/supplier.xml');
    interfaceB = xml2sts.readXML(filepath);
    dinapterpy = DinapterPy();
    buy = VectorElement("buy","OUT",["item"]);
    purchase = VectorElement("purchase","IN",["item"]);
    vector = Vector("filter-vector");
    vector.addElement(buy, "client");
    vector.addElement(purchase, "supplier");
    contracts = dinapterpy.generateContracts(interfaceA, interfaceB,[vector]);
    for contract in contracts:
        #print hash(contract);
        writeXML(contract.getName()+".xml", contract);
    print " -- The End --";

##
# It tests the sts2dinapter translator, Dinapter integration and later dinapter2contract translator.
#
def test():
    #filepath = os.path.join(os.path.abspath('.'), 'input/e001-ftp_tiny/client.bpel');
    #stsA = bpelparser.parse(filepath).aws2STS(1)[0];
    #filepath = os.path.join(os.path.abspath('.'), 'input/e001-ftp_tiny/server.bpel');
    #stsB = bpelparser.parse(filepath).aws2STS(1)[0];
    #interfaceA = Interface("left",mysts=stsA);
    #interfaceB = Interface("right",mysts=stsB);
    try:
        filepath = os.path.join(os.path.abspath('.'), 'input/e018d-SAC09/client.xml');
        interfaceA = xml2sts.readXML(filepath);
        filepath = os.path.join(os.path.abspath('.'), 'input/e018d-SAC09/supplier.xml');
        interfaceB = xml2sts.readXML(filepath);
        dinapterpy = DinapterPy();
        contracts = dinapterpy.generateContracts(interfaceA, interfaceB);
        for contract in contracts:
            #print hash(contract);
            writeXML(contract.getName()+".xml", contract);
        log.debug(" -- The End --");
    except Exception, msg:
        log.error("The test failed: %s" % msg);
        return 1;
    return 0;

##
# It tests sts2dinapter translator and Dinapter integration with JPype.
def testThere():
    filepath = os.path.join(os.path.abspath('.'), 'input/e018d-SAC09/client.xml');
    interfaceA = xml2sts.readXML(filepath);
    filepath = os.path.join(os.path.abspath('.'), 'input/e018d-SAC09/supplier.xml');
    interfaceB = xml2sts.readXML(filepath);
    dinapterpy = DinapterPy();
    engine = dinapterpy.runDinapter(interfaceA, interfaceB);
    print engine.getBestSolutionsMessage();
    dinapterpy.unloadDinapter();
    print "-- Finished! --";

##
# It tests the translator from STS interface to Dinapter internal model.
def testTranslator():
    filepath = os.path.join(os.path.abspath('.'), 'input/e018d-SAC09/supplier.xml');
    interface = xml2sts.readXML(filepath);
    jarpath = os.path.join(os.path.abspath('.'), 'dist');
    #startJVM(JVM_LIB);
    startJVM(JVM_LIB, "-Djava.ext.dirs=%s" % jarpath);
    transformer = STS2Dinapter();
    graph = transformer.sts2dinapter(interface);
    JFrame = JClass('javax.swing.JFrame');
    frame = JFrame('prueba');
    frame.setContentPane(graph.getGraphView());
    frame.pack();
    frame.setVisible(True);
    shutdownJVM();
    
##
# It tests whether Dinapter can be loaded or not and it executes its tiny default example.
def testDinapter():
    jarpath = os.path.join(os.path.abspath('.'), 'dist');
    #startJVM(JVM_LIB);
    startJVM(JVM_LIB, "-Djava.ext.dirs=%s" % jarpath);
    Dinapter = JClass('dinapter.Dinapter');
    java.lang.System.out.println("This seems to work");
    Dinapter.main(["tiny"]);
    shutdownJVM();
    
##
# It tests the cut global loop method.
def testCutGlobalLoops():
    filepath = os.path.join(os.path.abspath('.'), 'input/e019-WWW-MedRequest/server.xml');
    interfaceA = xml2sts.readXML(filepath);
    filepath = os.path.join(os.path.abspath('.'), 'input/e019-WWW-MedRequest/db.xml');
    interfaceB = xml2sts.readXML(filepath);
    dinapterpy = DinapterPy();
    sts2dinapter = STS2Dinapter(); 
    interfaceA = sts2dinapter.cutGlobalLoops(interfaceA);
    interfaceB = sts2dinapter.cutGlobalLoops(interfaceB);
    #xml2sts.writeXML("interfaceA.xml",interfaceA);
    #xml2sts.writeXML("interfaceB.xml",interfaceB);
    engine = dinapterpy.runDinapter(interfaceA, interfaceB);
    print engine.getBestSolutionsMessage();
    dinapterpy.unloadDinapter();
    print "-- Finished! --";    
    
##
# It tests the cut global loop method.
def testCutMiddleFinalStates():
    filepath = os.path.join(os.path.abspath('.'), 'input/e019-WWW-MedRequest/server.xml');
    interfaceA = xml2sts.readXML(filepath);
    filepath = os.path.join(os.path.abspath('.'), 'input/e019-WWW-MedRequest/db.xml');
    interfaceB = xml2sts.readXML(filepath);
    sts2dinapter = STS2Dinapter(); 
    interfaceA = sts2dinapter.cutMiddleFinalStates(interfaceA);
    interfaceB = sts2dinapter.cutMiddleFinalStates(interfaceB);
    xml2sts.writeXML("interfaceA.xml",interfaceA);
    xml2sts.writeXML("interfaceB.xml",interfaceB);
    print "-- Finished! --";
    
##
# It tests to discard solutions and look for different ones.
def testGenerateMoreContracts():
    filepath = os.path.join(os.path.abspath('.'), 'input/e018d-SAC09/client.xml');
    interfaceA = xml2sts.readXML(filepath);
    filepath = os.path.join(os.path.abspath('.'), 'input/e018d-SAC09/supplier.xml');
    interfaceB = xml2sts.readXML(filepath);
    dinapterpy = DinapterPy();
    dinapterpy.generateContracts(interfaceA, interfaceB);
    engine = dinapterpy._engine;
    print engine.getBestSolutionsMessage();
    print "Now discarding these solutions and looking for new ones...";
    dinapterpy.generateMoreContracts();
    print engine.getBestSolutionsMessage();
    dinapterpy.unloadDinapter();
    print "-- Finished! --";
    
##
# This is the Dinapter python interface. It's able to use ITACA default models.
class DinapterPy:
    
    ##
    # It initializes DinapterPy
    def __init__(self):
        self._engine = None;
        self._serviceA = None;
        self._serviceB = None;
        self._mandatoryVectors = None;
        self.__generatedContracts = [];
        self.__startTime = None;

    ##
    # Loads Dinapter classes into memory.
    def loadDinapter(self):
        global dinapterLoaded, Dinapter;
        if dinapterLoaded:
            return;
        jarpath = os.path.join(os.path.abspath(DINAPTERPY_PATH), 'dist');
        #startJVM(JVM_LIB);
        startJVM(JVM_LIB, "-Djava.ext.dirs=%s" % jarpath);
        Dinapter = JClass('dinapter.Dinapter');
        dinapterLoaded = True;
    
    ##
    # Unloads Dinapter classes.
    def unloadDinapter(self):
        global dinapterLoaded, Dinapter;
        if not dinapterLoaded:
            return;
        dinapterLoaded = False;
        Dinapter = None;
        shutdownJVM();
    
    ##
    # Executes Dinapter with the given two interfaces and returns Dinapter's engine.
    #
    # @param serviceA: Interface of the first service to adapt.
    # @param serviceB: Interface of the second service to adapt.
    # @param timeout: Timeout in seconds that, when reached, Dinapter will stop, reset, and return no contract. If it is 0 it will wait till Dinapter ends.
    # @return: Dinapter's engine after the specification process has completed.
    # @defreturn JSearchSpecificator. 
    def runDinapter(self,serviceA, serviceB,similarities=[],timeout=0):
        global Dinapter;
        self.loadDinapter();
        sts2dinapter = STS2Dinapter();
        componentA = sts2dinapter.sts2dinapter(serviceA);
        componentB = sts2dinapter.sts2dinapter(serviceB);
        compatibilityFacts = self._generateCompatibilityFacts(componentA, componentB,similarities);
        #print compatibilityFacts;
        #if log.isEnabledFor(logging.DEBUG):
        #    log.debug("SimTool compatibility measures: \n"+str(compatibilityFacts));
        self.__startTime = time();
        return Dinapter.generateSpecifications(componentA, componentB, compatibilityFacts, timeout);
    
    def _generateCompatibilityFacts(self,componentA, componentB, similarities):
        #print similarities
        activities = [];
        for component in [componentA, componentB]:
            act = {};
            activities.append(act);
            for node in component.getAllNodes():
                type = None;
                if node.getNodeType() == "SEND":
                    type = "OUT";
                elif node.getNodeType() == "RECEIVE":
                    type = "IN";
                else:
                    continue;
                name = node.getDescription();
                act[(name,type)] = node;
        facts = [];
        if log.isEnabledFor(logging.DEBUG):
            log.debug("SimTool compatibility measures:");
        for similarity in similarities:
            #print similarity
            if log.isEnabledFor(logging.DEBUG):
                log.debug(str(similarity));
            facts.append((activities[0][similarity[0]], activities[1][similarity[1]], similarity[2]));
        objectClass = JClass('java.lang.Object');
        toReturn = JArray(objectClass,2)(len(facts));
        for i in range(len(facts)):
            toReturn[i] = JArray(objectClass)(facts[i]);
        # The following shows the contents of toReturn
        #for i in range(len(toReturn)):
        #    for j in range(len(toReturn[i])):
        #        print toReturn[i][j]
        return toReturn;
    
    ##
    # Executes Dinapter with the given two interfaces and returns a list of contracts.
    #
    # @param serviceA: Interface of the first service to adapt.
    # @param serviceB: Interface of the second service to adapt.
    # @param cutGlobalLoops: Cuts global loops because Dinapter doesn't support middle final states.
    # @param cutMiddleFinalStates: Cuts transitions outgoing from final states because Dinapter doesn't support middle final states. 
    # @param mandatoryVectors: Vectors which are mandatory in the returned contracts.
    # @param timeout: Timeout in seconds that, if reached, Dinapter will stop, reset, and return no contracts. 
    # @return: List of generated contracts imported from Dinapter.
    # @defreturn List of contracts. 
    def generateContracts(self,serviceA, serviceB, cutGlobalLoops=True, cutMiddleFinalStates=False, mandatoryVectors=[], timeout=DEFAULT_TIMEOUT):
        global _removedPlaceholders;
        sts2dinapter = STS2Dinapter();
        _removedPlaceholders = {};
        if DISABLE_SIM:
            if USE_WORDNET:
                similarities = self._generateWordNetSimilarities(serviceA,serviceB);
            else:
                similarities = [];
        else:
            similarities = self._generateSimilarities(serviceA,serviceB);
        if cutGlobalLoops:
            serviceA = sts2dinapter.cutGlobalLoops(serviceA);
            serviceB = sts2dinapter.cutGlobalLoops(serviceB);
        if cutMiddleFinalStates:
            serviceA = sts2dinapter.cutMiddleFinalStates(serviceA);
            serviceB = sts2dinapter.cutMiddleFinalStates(serviceB);
        # If service interfaces are too big, ignore irremediable arguments in order \
        # to Dinapter to finish
        (serviceA, serviceB) = self._removeUnmatchedArguments(serviceA, serviceB);
        # ---
        self._serviceA = serviceA;
        self._serviceB = serviceB;
        self.__generatedContracts = [];
        self.__startTime = time();
        self._engine = self.runDinapter(serviceA, serviceB,similarities,timeout);
        self._mandatoryVectors = mandatoryVectors;
        # @TODO: Related to mandatoryVectors we have tickets #41 and #45.
        return self._importAndFilterDinapterContracts(serviceA, serviceB, mandatoryVectors,self._engine);
    
    def _removeUnmatchedArguments(self, interfaceA, interfaceB):
        global _removedPlaceholders;
        # @TODO: This should have been done through expert system rules. 
        stsA = interfaceA.getSTS().copy();
        stsB = interfaceB.getSTS().copy();
        if (len(stsA.getTransitions()) <= UNMATCHED_TRANSITIONS_TREASHOLD) and \
                (len(stsB.getTransitions()) <= UNMATCHED_TRANSITIONS_TREASHOLD):
            # Don't remove anything if they are small.
            return interfaceA, interfaceB;
        labelsA = stsA.getLabels().values();
        labelsB = stsB.getLabels().values();
        toRemove = set();
        dataA = set();
        dataB = set();
        for labelA in labelsA:
            # @TODO: I assume every label is either input or output.
            dataA |= set(labelA.getData());
        for labelB in labelsB:
            dataB |= set(labelB.getData());
        toRemove = dataA ^ dataB;
        toProcess = [];
        if len(stsA.getTransitions()) > UNMATCHED_TRANSITIONS_TREASHOLD:
            log.debug("Ignoring unmatched arguments in %s." % interfaceA.getName());
            toProcess += labelsA;
        if len(stsB.getTransitions()) > UNMATCHED_TRANSITIONS_TREASHOLD:
            log.debug("Ignoring unmatched arguments in %s." % interfaceB.getName());
            toProcess += labelsB;
        for label in toProcess:
            labelData = label.getData();
            newData = [];
            missingPlaceholder = [];
            i = 0;
            for data in labelData:
                if data not in toRemove:
                    newData.append(data);
                else:
                    missingPlaceholder.append((i,data));
                i += 1;
            if (len(newData) > 0) and (len(missingPlaceholder) > 0):
                key = (label in labelsA, label.getName(), label.getType()); 
                if key in _removedPlaceholders:
                    # @TODO: This shouldn't be like this.
                    raise Exception, "DinapterPy doesn't support these interfaces.";
                # @TODO: What happend if there are the same labels
                _removedPlaceholders[key] = missingPlaceholder;
            label.setData(newData);
        iA = copy(interfaceA);
        iA.setSTS(stsA);
        iB = copy(interfaceB);
        iB.setSTS(stsB);
        return iA, iB;
    
    ##
    # It generates similarities using WordNet
    # @param interfaceA: Interface to compare.
    # @param interfaceB: Another interface to compare.
    # @param type: Type of measure for the similarity. See http://marimba.d.umn.edu/similarity/measures.html
    # @param type: Maximum value of the similarity returned by Wordnet::Similarity.
    # @return: A list with tuples which represent the different compatibility measures among the operations.   
    def _generateWordNetSimilarities(self, interfaceA, interfaceB, type='res', maxValue=1., evenlyDistributed=True):
        log.info('Querying WordNet-Similarity. This will take a couple of minutes...');
        interfaceName = interfaceA.getName();
        interfaces = {interfaceA.getName():interfaceA, interfaceB.getName():interfaceB};
        sts = interfaceA.getSTS();
        stsb = interfaceB.getSTS();
        similarities = [];
        labels = [label.getName() for label in sts.getLabels().values()+stsb.getLabels().values()];
        try:
            labels = filter(self._isWordInWordNet, labels);
        except IOError:
            log.error('There is no access to the WordNet web service.');
            return similarities;
        # For not to repeat the no access error message.
        PATTERN = 'using %s is ' % str(type);
        for transitionA in interfaceA.getSTS().getTransitions():
            if transitionA.isTau(): continue;
            labelA = sts.getLabel(transitionA.getLabel());
            if labelA.getName() not in labels: continue;
            for transitionB in interfaceB.getSTS().getTransitions():
                if transitionB.isTau(): continue;
                labelB = stsb.getLabel(transitionB.getLabel());
                if labelB.getName() not in labels: continue;
                similarity = None;
                if labelA.getType() != labelB.getType():
                    # @TODO: Parse camelCase, replace strange symbols by spaces, and so on...
                    sleep(1); # @TODO: I don't like to wait but it seems that we overloaded the server.
                    operationA=labelA.getName();
                    operationB=labelB.getName();
                    #log.debug('Similarity between %s and %s...' % (operationA, operationB));
                    url = str('http://marimba.d.umn.edu/cgi-bin/similarity/similarity.cgi?' + \
                            'word1=%s&senses1=all&word2=%s&senses2=all&measure=%s&rootnode=yes' \
                            % (str(operationA), str(operationB), str(type)))
                    #log.debug(url);
                    try:
                        response = urlopen(url).read();
                        index = response.find(PATTERN);
                        if index != -1:
                            index = index+len(PATTERN);
                            response = response[index:response.find('.</p>',index)];
                            try:
                                similarity = float(response);
                                if maxValue != 1.:
                                    similarity = similarity/maxValue;
                                log.debug('WordNet-Similarity between "%s" and "%s" is %r.' % (operationA, operationB, similarity));
                            except:
                                log.error('Error parsing the similarity response. Using %r by default.' % similarity);
                        else:
                            log.debug('Either "%s" or "%s" were not found in WordNet.' % (operationA, operationB));
                    except IOError:
                        log.error('There is no access to WordNet-Similarity web service.');
                        return similarities;
                if similarity != None:
                    similarities.append((self.__processLabel(labelA)
                            , self.__processLabel(labelB), similarity));
        log.info('WordNet-Similarity complete.');
        if evenlyDistributed:
            valueList = map(lambda x: x[2],similarities);
            distribution = self._evenlyDistribute(valueList);
            similarities = map(lambda x: (x[0],x[1],distribution[x[2]]),similarities);
        return similarities;
    
    ##
    # It queries WordNet for the given word and returns whether it exists or not.
    # If this method must be called several times, it should include a delay (~ 1s.) between calls.
    #
    # @param word: Word to look for.
    # @param delay: The time in seconds it will wait before querying WordNet. 
    # @return: True if, and only if, the word exists in WordNet. 
    def _isWordInWordNet(self,word,delay=1):
        sleep(delay);
        url = 'http://wordnetweb.princeton.edu/perl/webwn?s=%s&sub=Search+WordNet&o2=&o0=1&o7=&o5=&o1=1&o6=&o4=&o3=&h=000000' % str(word);
        toReturn = urlopen(url).read().find('Your search did not return any results.') == -1;
        #log.debug('Is "%s" in WordNet?: %r' % (word,toReturn));
        return toReturn;
    
    ##
    # It generates the similarities between the operations of the given interfaces.
    #
    # @param interfaceA: Interface to compare.
    # @param interfaceB: Another interface to compare.
    # @param evenyDistributed: Whether these measures should be use as they are (False) or they must be evenly distributed [0,100].
    # @return: A list with tuples which represent the different compatibility measures among the operations.   
    def _generateSimilarities(self,interfaceA, interfaceB, evenlyDistributed=False):
        interfaceName = interfaceA.getName();
        interfaces = {interfaceA.getName():interfaceA, interfaceB.getName():interfaceB};
        sts = interfaceA.getSTS();
        stsb = interfaceB.getSTS();
        similarities = [];
        for transition in interfaceA.getSTS().getTransitions():
            if transition.isTau(): continue;
            for (a,b,s) in sim.computeSimilarityByTransition(interfaces, transition, interfaceName, SIM_WORDNET, SIM_INTRANSITION):
                if a.isTau() or b.isTau(): continue;
                similarities.append((self.__processLabel(sts.getLabel(a.getLabel()))
                        ,self.__processLabel(stsb.getLabel(b.getLabel())),s));
        # Now we replace duplicates by their mean value.
        toReturn = [];
        for similarity in similarities:
            value = 0;
            count = 0;
            for simB in similarities:
                if similarity[0:2] == simB[0:2]:
                    value += simB[2];
                    count += 1;
            toReturn.append((similarity[0],similarity[1],value/count));
        if evenlyDistributed:
            valueList = map(lambda x: x[2],toReturn);
            distribution = self._evenlyDistribute(valueList);
            toReturn = map(lambda x: (x[0],x[1],distribution[x[2]]),toReturn);
        return toReturn;
            
    ##
    # It returns a dictionary with the list elements as keys and their evenly 
    # distributed counterparts as dictionary values.
    #
    # @param valueList: A list of values to evenly distribute.
    # @return: A dictionary with the new distribution of values. 
    def _evenlyDistribute(self,valueList):
        values = set(valueList);
        if len(values) == 0:
            return {};
        elif len(values) == 1:
            return {valueList[0]:0.5};
        difference = 1./(len(values)-1);
        toReturn = {};
        ac = 0.;
        for value in sorted(values):
            toReturn[value] = min(ac,1.); # To avoid numbers above 1.
            ac += difference; 
        return toReturn;
    
    ##
    # It generates a tuple with the name of the label and its type.
    #
    # @param label: The label to process.
    # @return: Tuple with the name and type of the label. 
    def __processLabel(self,label):
        return (label.getName(),label.getType());
    
    ##
    # It skips the current generated solutions and tries to generate new ones.
    #
    # @param timeout: The number of seconds Dinapter is allowed to work before being interrupted and reset. It will return no contract in this case. 
    # @return: The list of generated contracts.
    def generateMoreContracts(self,timeout=DEFAULT_TIMEOUT):
        if self._engine == None:
            raise Exception, "You have to generateContracts before calling generateMoreContracts.";
        if self._engine.isSuccessfullyCompleted():
            if self.__startTime == None:
                self.__startTime = time();
            elif timeout != long(0):
		log.debug("time %r ; start %r ; timeout %r" % (time(),self.__startTime, timeout));
                timeout = long(self.__startTime + timeout - time());
                if timeout <= 0:
                    log.debug("Timeout elapsed");
                    return [];
            self._engine.findDifferentSolutions(timeout);
            return self._importAndFilterDinapterContracts();
        else:
            log.error("Dinapter didn't complete properly so no more contracts can be generated.");
            return [];
    
    ##
    # It gets the best solutions found by Dinapter, it converts them to ITACA contracts and filters them using the given mandatoryVectors.
    #
    # @param serviceA: Interface of the first service to adapt.
    # @param serviceB: Interface of the second service to adapt.
    # @param mandatoryVectors: Vectors which are mandatory in the returned contracts.
    # @param engine: Dinapter engine (JSearchSpecificator).
    # @param filterByEquivalentVectors: Filters contracts whose vectors (not vLTSs) are equivalents to the vectors of another contract.
    # @param reiterateTillSolution: If True, it won't return until it finds a specification, at least, which includes the mandatoryVectors. If False, it will return at the end of the iteration possibly returning zero specifications.  
    # @return: List of generated contracts imported from Dinapter.
    def _importAndFilterDinapterContracts(self,serviceA=None
                                          , serviceB=None
                                          , mandatoryVectors=None
                                          , engine=None
                                          , filterByEquivalentVectors=True
                                          , reiterateTillSolution=True):
        if (serviceA == None): serviceA=self._serviceA;
        if (serviceB == None): serviceB=self._serviceB;
        if (mandatoryVectors == None): mandatoryVectors=self._mandatoryVectors;
        if (engine == None): engine=self._engine;
        if log.isEnabledFor(logging.DEBUG):
            log.debug("Generating contracts with these mandatory vectors: %s" % str(mandatoryVectors))
        importer = Dinapter2Contract();
        contractList = [];
        if log.isEnabledFor(logging.DEBUG):
            log.debug(engine.getStatusMessage());
        for dinapterSpecification in engine.getBestSolutions():
            if log.isEnabledFor(logging.DEBUG):
                log.debug("--------------------\n" + dinapterSpecification.toString());
            contract = importer.dinapter2contract(dinapterSpecification, \
                                                     serviceA.getName(), \
                                                     serviceB.getName());
            appendContract = True;
            if (len(mandatoryVectors) > 0) and \
                    not(self._containsMandatoryVectors(mandatoryVectors,contract)):
                log.debug("Discarded one contract because it didn't contain the mandatory vectors."); 
                appendContract = False;
            # @TODO: Ideally, the filter should be over equivalent contracts, nor their vectors.
            # In other words, vLTSs must be compared as well. 
            if (appendContract and filterByEquivalentVectors):
                for con in self.__generatedContracts:
                    if con.containsEquivalentVectors(contract.getVectors().values()):
                        log.debug("Discarded one contract because there was another one with equivalent vectors.");
                        appendContract = False;
                        break;
            if appendContract:
                contractList.append(contract);
                self.__generatedContracts.append(contract);
        #Using mandatoryVectors, if no proper contract is found, discard current solutions and keep searching.
        if not(reiterateTillSolution) or (len(contractList) != 0) or \
                not(engine.isSuccessfullyCompleted()):
            self.__startTime = None;
            return contractList;
        else:
            return self.generateMoreContracts();

    ##
    # It checks if the given contract contains the given mandatory vectors.
    # Placeholders will be matched accordingly between the contract and the mandatory vectors.
    #
    # @param mandatoryVectors: Vectors which must be present in the contract.
    # @param contract: Contract where the mandatory vectors are going to be looked for.
    # @param boundedPlaceholders: As this method is recursive, this is the dictionary of the bounded placeholders.
    # @return: True if, and only if, all the mandatory vectors are present within the contract.   
    def _containsMandatoryVectors(self, mandatoryVectors, contract, boundedPlaceholders=None):
        if len(mandatoryVectors) == 0:
            return True;
        if boundedPlaceholders == None:
            boundedPlaceholders = {};
        mVector = mandatoryVectors[0];
        toReturn = False;
        for vector in contract.getVectors().values():
            newBoundedPlaceholders = self.__vectorsMatch(mVector, vector, boundedPlaceholders);
            if newBoundedPlaceholders != None:
                toReturn |= self._containsMandatoryVectors(
                          mandatoryVectors[1:]
                        , contract
                        , newBoundedPlaceholders);
                if toReturn: return toReturn;
        return toReturn;
    
    ##
    # It returns whether the two given vectors can be matched regarding the given bounded
    # placeholders and it returns a dictionary with all the bounded placeholders required.
    #
    # @param mVector: Vector to be matched.
    # @param vector: The other vector to be matched.
    # @param boundedPlaceholders: Placeholders from vector matched against arguments of mVector.
    # @return: None if the vectors cannot be matched or the dictionary of the bounded placeholders required for both vectors to be matched.  
    def __vectorsMatch(self,mVector,vector,boundedPlaceholders):
        if set(mVector.getElements().keys()) != set(vector.getElements().keys()):
            return None;
        toReturn = dict(boundedPlaceholders);
        for key in mVector.getElements().keys():
            elem = vector.getElement(key);
            mElem = mVector.getElement(key);
            if (mElem.getType() != elem.getType()) or \
                    (elem.getName() != mElem.getName()):
                return None;
            data = elem.getData();
            mData = mElem.getData();
            # @TODO: Review this. What happens if the mandatory vector has placeholders not used between these two services?
            #if len(data) != len(mData):
            #    return None;
            for i in range(len(data)):
                if data[i] in toReturn:
                    if toReturn[data[i]] != mData[i]:
                        return None;
                else:
                    toReturn[data[i]] = mData[i];
        return toReturn;
        
            
##
# Exception raised when the given STS model cannot be translated into Dinapter's internal model.
class STSNotSupported(Exception):
    pass;

##
# Translator from STS Interfaces to Dinapter's internal model.
class STS2Dinapter():

    ##
    # Translates the given STS interface into Dinapter's internal model.
    # @param interface: The STS interface to be translated.
    # @param view: If True, it displays a windows with the returned behavior.
    # @return: Dinapter's internal model for the given interface.
    # @defreturn: JPowerBehaviorGraph.
    def sts2dinapter(self,interface, view=False):
        log.debug("Translating %s from STS to Dinapter internal model..." % interface.getName());
        self.__interface = interface;
        sts = interface.getSTS();
        self.__activities = dict();
        state = sts.getInitial();
        DinapterBuilder = JClass('dinapter.behavior.JPowerBehaviorGraphBuilder');
        self.BehaviorNodeType = JClass('dinapter.behavior.BehaviorNode$BehaviorNodeType');
        self.__builder = DinapterBuilder();
        self.__builder.createNewGraph();
        self.__exitActivity = self.__builder.createNode(self.BehaviorNodeType.EXIT,[]);
        startType = self.BehaviorNodeType.START;
        previousActivity = self.__builder.createNode(startType,[]);
        #self._activities[state]=previousActivity; 
        self.state2dinapter(previousActivity,state);
        toReturn = self.__builder.getGraph();
        if view:
            JFrame = JClass('javax.swing.JFrame');
            Dimension = JClass('java.awt.Dimension');
            frame = JFrame("Behavior of "+interface.getName());
            frame.getContentPane().add(toReturn.getGraphView());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setPreferredSize(Dimension(512, 600));
            #frame.setLocation(0, 0);
            frame.pack();
            frame.setVisible(True);
        return toReturn;
    
    ##
    # It translates an STS state into Dinapter's internal model.
    # @param previous: Dinapter's node where the new translation must be linked from.
    # @param state: STS state to be translated.
    def state2dinapter(self,previous,state):
        sts = self.__interface.getSTS();
        transitions = sts.outgoingTransitions(state);
        log.debug("In state2dinapter [transitions:%u]" % len(transitions));
        if state in self.__activities:
            log.debug("Loop found: state already translated. [state:%s]" % state);
            self.__builder.link(previous,self.__activities[state]);
        elif sts.getState(state).isFinal():
            if len(sts.outgoingTransitions(state)) != 0:
                raise STSNotSupported, "Dinapter does not support final states with outgoing transitions [state:%s]. Try cutting global loops and middle final states." \
                                    % state;
            exitActivity = self.__exitActivity;
            if state not in self.__activities:
                self.__activities[state] = exitActivity;
            self.__builder.link(previous,exitActivity);
        elif len(transitions) == 1:
            self.transition2dinapter(previous,state,transitions[0]);
        elif self.isIf(state):
            self.if2dinapter(previous,state);
        elif self.isPick(state):
            self.pick2dinapter(previous,state);
        else:
            raise STSNotSupported, "The structure of this STS is not supported by Dinapter [state:%s]" \
                                 % state;
    
    ##
    # It translates an STS transition into Dinapter's internal model.
    # @param previous: Dinapter's node where the new translation must be linked from.
    # @param state: Source state of the transition.
    # @param transition: STS transition to translate.   
    def transition2dinapter(self,previous,state,transition):
        log.debug("In transition2dinapter");
        if transition.isTau():
            self.state2dinapter(previous,transition.getTarget());
        else:
            label = self.__interface.getSTS().getLabel(transition.getLabel());
            arguments = label.getData();
            type = self.BehaviorNodeType.RECEIVE;
            if label.isOutput():
                type = self.BehaviorNodeType.SEND;
            name = label.getName();
            activity = self.__builder.createNode(name,type,arguments);
            if state not in self.__activities:
                self.__activities[state] = activity;
            self.__builder.link(previous,activity);
            self.state2dinapter(activity,transition.getTarget());
    
    ##
    # It translates an "if" STS structure into Dinapter's internal model.
    # An "if" STS structure begins with a state with several branches which, once all
    # the taus have been ignored, every one of its transitions has an OUT
    # label.
    #
    # @param previous: Dinapter's node where the new translation must be linked from.
    # @param state: Root state of the "if" STS structure.
    def if2dinapter(self,previous,state):
        log.debug("In if2dinapter");
        transitions = list(self.getNotTaus(state)); #self.__interface.getSTS().outgoingTransitions(state);
        ifActivity = self.__builder.createNode(self.BehaviorNodeType.IF,[]);
        self.__builder.link(previous,ifActivity);
        self.__activities[state]=ifActivity;
        for transition in transitions:
            #self.transition2dinapter(ifActivity,state,transition);
            self.state2dinapter(ifActivity, transition.getSource());
    
    ##
    # It translates a "pick" STS structure into Dinapter's internal model.
    # A "pick" STS structure begins with a state with several outgoing transitions and
    # every one of those transitions has an "IN" label.
    #
    # @param previous: Dinapter's node where the new translation must be linked from.
    # @param state: Root state of the "pick" STS structure. 
    def pick2dinapter(self,previous,state):
        transitions = self.__interface.getSTS().outgoingTransitions(state);
        pickActivity = self.__builder.createNode(self.BehaviorNodeType.PICK,[]);
        self.__builder.link(previous,pickActivity);
        self.__activities[state]=pickActivity;
        for transition in transitions:
            self.transition2dinapter(pickActivity,state,transition);
    
    ##
    # It returns whether or not the given state is the root state of an "pick" STS structure.
    # A "pick" STS structure begins with a state with several outgoing transitions and
    # every one of those transitions has an "IN" label.
    #
    # @param state: Possible root state of the "pick" STS structure.
    # @return: Whether or not the given state is root of a "pick" STS structure.
    def isPick(self,state):
        sts = self.__interface.getSTS();
        transitions=sts.outgoingTransitions(state)
        toReturn = len(transitions) > 0 and \
            len(filter(lambda x: x.isTau() or sts.getLabel(x.getLabel()).isOutput() ,transitions)) == 0;
        log.debug("In isPick [state:%s,transitions:%u,isPick:%r" % (state,len(transitions),toReturn));
        return toReturn;
    
    ##
    # It returns whether or not the given state is the root state of an "if" STS structure.  
    # An "if" STS structure begins with a state with several branches which, once all
    # the taus have been ignored, every one of its transitions has an OUT
    # label.
    #
    # @param previous: Dinapter's node where the new translation must be linked from.
    # @param state: Root state of the "if" STS structure.
    # @param ignoreFirstLevelTaus: If True, it recognizes states with several output outgoing transitions as "if"s. \
    #                              Otherwise, it requires the first level of outgoing transitions to be taus.
    # @return: Whether or not the given state is root of a "if" STS structure.
    def isIf(self,state,ignoreFirstLevelTaus=False):
        transitions=self.__interface.getSTS().outgoingTransitions(state)
        sts = self.__interface.getSTS();
        if (ignoreFirstLevelTaus):
            toReturn = not self.isPick(state);
        else:
            toReturn = len(filter(lambda x: not x.isTau(), transitions)) == 0;
        return toReturn and \
            len(filter(lambda x: sts.getLabel(x.getLabel()).isInput(), self.getNotTaus(state))) == 0;
    
    ##
    # It returns the set of the first transitions found from this state which are not taus.
    #
    # @param state: The state where the transition search must be started.
    # @param filter: Set of states already visited in order to ignore them. For loops, because of recursion.
    # @return: A set of no-tau transitions. 
    def getNotTaus(self,state,filter=None):
        if filter == None:
            filter = set();
        toReturn = set();
        if state in filter:
            return toReturn;
        filter.add(state)
        for transition in self.__interface.getSTS().outgoingTransitions(state):
            if transition.isTau():
                toReturn |= self.getNotTaus(transition.getTarget(),filter);
            else:
                toReturn.add(transition);
        return toReturn;
    
    ##
    # It cuts global loops in the STS because Dinapter doesn't support final/stable states with outgoing transitions. 
    # Even less when it's the initial state!
    #
    # @param interface: Interface whose STS is going to be copied and changed in order to remove global loops.
    # @return: A new interface similar to the given one but without global loops.
    def cutGlobalLoops(self,interface):
        sts = interface.getSTS();
        initial = sts.getInitial();
        if not (initial in sts.getFinals()):
            # There are no global loops so nothing is changed.
            return interface;
        log.debug("Cutting global loops in the interface: %r" % interface.getName());2
        #toReturn = deepcopy(interface);
        toReturn = copy(interface);
        sts = sts.copy();
        toReturn.setSTS(sts); 
        initial = sts.getInitial();
        sts.getState(initial).setFinal(False);
        sts.removeFinal(initial);
        # @TODO: Customize the name for the new final state.
        newFinalState = State("NO_GLOBAL_LOOP_FINAL",final=True);
        sts.addState(newFinalState);
        newFinalStateName=newFinalState.getName();
        sts.addFinal(newFinalStateName);
        for transition in sts.incomingTransitions(initial):
            sts.addTransition(Transition(transition.getSource(),transition.getLabel(),newFinalStateName));
            sts.removeTransition(transition);
        return toReturn;
    
    ##
    # It cuts transitions going out of final states because Dinapter doesn't support them.
    #
    # @param interface: Interface whose STS is going to be copied and changed in order to cut transitions in middle final states.
    # @return: A new interface with no final states with outgoing transitions. 
    def cutMiddleFinalStates(self,interface):
        areThereMiddleFinalStates = False;
        sts = interface.getSTS();
        for state in sts.getFinals():
            if len(sts.outgoingTransitions(state)) > 0:
                areThereMiddleFinalStates = True;
                break;
        if not areThereMiddleFinalStates:
            # There aren't any middle final states so nothing is changed.
            return interface;
        log.debug("Removing middle final states in the interface: %r" % interface.getName());
        #toReturn = deepcopy(interface);
        toReturn = copy(interface);
        sts = sts.copy();
        toReturn.setSTS(sts);
        initial = sts.getInitial();
        self._cutMiddleFinalStates(initial,sts,set());
        return toReturn;
        
        
    ##
    # It cuts transitions going out of final states because Dinapter doesn't support them.
    #
    # @param state: Name of the state where the search is going to start from.
    # @param lts: LTS which contains the given state. 
    # @return: A new interface with no final states with outgoing transitions from the given state. 
    def _cutMiddleFinalStates(self,state,lts,visited):
        if (state in visited):
            return; # Stop on loops.
        else:
            visited.add(state);
        if state in lts.getFinals():
            for transition in lts.outgoingTransitions(state): 
                self._removeTransitionAndUnconnectedDescendant(transition,lts);
        else:
            for transition in lts.outgoingTransitions(state):
                self._cutMiddleFinalStates(transition.getTarget(), lts, visited);
            
        
    ##
    # It removes the given transition and any other state which might be unconnected after the removal.
    #
    # @param transition: Transition to remove.
    # @param lts: LTS which contains the given transition.
    def _removeTransitionAndUnconnectedDescendant(self,transition,lts):
        log.debug("Removing transition: %r" % transition)
        target = transition.getTarget();
        lts.removeTransition(transition);
        if len(lts.incomingTransitions(target)) == 0:
            for trans in lts.outgoingTransitions(target):
                self._removeTransitionAndUnconnectedDescendant(trans,lts);
            lts.removeState(target);
            

##
# This class imports Dinapter specifications into INCA contracts.
class Dinapter2Contract:
    
    ## Instatiator.
    def __init__(self):
        self.__BehaviorNodeType = JClass('dinapter.behavior.BehaviorNode$BehaviorNodeType');
        self.__counter = 1;
        self.__contractNumber = 1;
        
    ##
    # Translates a Dinapter specification into an INCA contract.
    # @param dinapterSpec: Dinapter specification to translate.
    # @param leftComponent: Name of the left service.
    # @param rightComponent: Name of the right service.
    # @return: INCA contract.   
    def dinapter2contract(self,dinapterSpec,leftComponent,rightComponent,useTrivialVLTS=not(GENERATE_VLTS)):
        self.__stateCounter = -1;
        self.__counter = 1;
        self.__leftName = leftComponent;
        self.__rightName = rightComponent;
        vectors = set();
        lts = LTS();
        state = self._createState();
        state.setFinal(True);
        state.setInitial(True);
        lts.addState(state);
        lts.setInitial(state.getName());
        lts.addFinal(state.getName());
        for rule in dinapterSpec.getRules():
            vectors |= self.rule2vectors(rule,lts);
        contractNumber = long(time()) * self.__contractNumber * 5;
        contractNumber = int(contractNumber%1000);
        contract = Contract("c_%u" % contractNumber);
        self.__contractNumber += 1;
        for vector in vectors:
            contract.addVector(vector);
        if useTrivialVLTS:
            contract.setLTS(self._createTrivialvLTS(contract));
        else:
            contract.setLTS(lts);
        return contract;
    
    ##
    # It translates a Dinapter rule into vectors.
    # @param rule: Dinapter rule to translate.
    # @return: Set of vectors. 
    def rule2vectors(self,rule,lts):
        # These generates python lists from java arrays.
        left = [activity for activity in rule.getLeftSide()];
        right = [activity for activity in rule.getRightSide()];
        deps = self.dependencies(left,right);
        # -- Generate a placeholder dictionary for later use. --
        placeholders = {};
        argumentsDict = {};
        for activity in left+right:
            if (activity in placeholders) or (len(activity.getArguments()) == 0):
                continue;
            else:
                arguments = [];
                for argument in activity.getArguments():
                    placeholder = argumentsDict.get(argument);
                    if placeholder == None:
                        # No previous placeholder was found, create a new one.
                        placeholder = self.__generatePlaceholder(left,right,argument);
                        argumentsDict[argument] = placeholder;
                        placeholders[argument] = placeholder;
                    arguments.append(placeholder);
                placeholders[activity] = arguments;
        # -== End of placeholder generation ==-
        vectors = self.sequences2vectors(left,right,deps,placeholders);
        self.sequenceVectors2lts(left, right, list(vectors), lts, lts.getInitial());
        return vectors;
    
    ##
    # It generates a unique placeholder identifier.
    # Placeholders are generated based on the hash value of the rule
    # and the given argument in order to easy the equality check
    # between contracts.
    #
    # @param left: Left sequence of activities of the rule where the placeholder is going to be used.
    # @param right: Right sequence of activities of the rule where the placeholder is going to be used.
    # @param argument: The identifier of the argument the placeholder is going to be used for. 
    # @return: Placeholder indentifier.
    def __generatePlaceholder(self, left, right, argument):
        hash = long("7");
        for activity in filter(lambda x: argument in x.getArguments(), left+right):
            activityHash = activity.hashCode();
            hash = hash * activityHash;
        hash += argument.__hash__();
        hash = int(hash % 10000);
        toReturn = "P_%i" % hash;
        return toReturn;
    
    ##
    # It translates two sequences of activities into vectors.
    # @param left: Left sequence of activities.
    # @param right: Right sequence of activities.
    # @param dependencies: Dependencies among the activities of the two sequences.
    # @return: A set of vectors.   
    def sequences2vectors(self,left,right,dependencies,placeholders):
        deps = filter(lambda x: (x[1] in left) and (x[2] in right),dependencies);
        if len(deps) > 0:
            matches,la,ra = deps[0];
            vector = self.createVector(placeholders, left=la, right=ra);
            #log.debug("Vector dict: %r" % vector.getElements());
            #log.debug("Vector hash: %r" % hash(vector));
            pLeftActions, aLeftActions = self.splitSequence(la,left);
            pRightActions, aRightActions = self.splitSequence(ra,right);
            toReturn = self.sequences2vectors(pLeftActions,pRightActions,deps[1:],placeholders);
            toReturn |= self.sequences2vectors(aLeftActions,aRightActions,deps[1:],placeholders);
            toReturn.add(vector);
            return toReturn;
        else:
            toReturn = [];
            for a in left:
                toReturn.append(self.createVector(placeholders, left=a));
            for a in right:
                toReturn.append(self.createVector(placeholders, right=a));
            return set(toReturn);
    
    ##
    # It includes the proper vLTS transitions for the given sequence of actions and
    # synchronization vectors.
    #
    # @param left: Left list of actions.
    # @param right: Right list of actions.
    # @param lts: LTS where the transitions are going to be included.
    # @param state: The name of the LTS state where the transitions come from.   
    def sequenceVectors2lts(self,left,right,vectors,lts,state):
        for vector in self._appliableVectors(left, right, vectors):
            processed = False;
            vs = vectors[:];
            vs.remove(vector);
            ls,rs = self._removeVectorFromSequences(left,right,vector);
            for transition in lts.outgoingTransitions(state):
                if transition.getLabel() == vector.getName():
                    self.sequenceVectors2lts(ls,rs,vs,lts,state);
                    processed = True;
                    break;
            if processed: continue;
            target = lts.getInitial(); 
            if (len(ls) != 0) or (len(rs) != 0):
                newState = self._createState();
                target = newState.getName();
                lts.addState(newState);
            transition = Transition(state,vector.getName(),target);
            #transition = Transition(state,NLabel(vector.getName()),target);
            lts.addTransition(transition);
            self.sequenceVectors2lts(ls,rs,vs,lts,target);
    
    ##
    # It creates a state with an unique name.
    #
    # @return: A new state.
    def _createState(self):
        self.__stateCounter += 1;
        return State("s_%u" % self.__stateCounter);
    
    
    ##
    # Removes the vector actions from the beginning of the given sequences.
    #
    # @param left: Left list of actions.
    # @param right: Right list of actions.
    # @param vector: Vector to be applied to the sequences.
    # @return: Tuple with the new two sequences.   
    def _removeVectorFromSequences(self,left,right,vector):
        if not self._canVectorBeApplied(left,right,vector):
            raise Exception, "The given vector cannot be applied.";
        l = left;
        r = right;
        if self.__leftName in vector.getElements():
            l = left[1:];
        if self.__rightName in vector.getElements():
            r = right[1:];
        return l,r;
    
    
    ##
    # It returns a list of appliable vectors from the given list.
    # @param left: Left list of actions.
    # @param right: Right list of actions.
    # @param vectors: Vectors to be filtered by their applicability.
    # @return: A list of applicable vectors.
    def _appliableVectors(self,left,right,vectors):
        return filter(lambda vector: self._canVectorBeApplied(left, right, vector), vectors);
        
    ##
    # It returns whether the given vector can be applied or not to the given sequences of actions.
    # @param left: Left list of actions.
    # @param right: Right list of actions.
    # @param vector: Vector to be applied.
    # @return: True if the vector is appliable.
    def _canVectorBeApplied(self,left,right,vector):
        if self.__leftName in vector.getElements():
            if (len(left) == 0) or (not self._isActivityElement(left[0],vector.getElement(self.__leftName))):
                return False;
        if self.__rightName in vector.getElements():
            if (len(right) == 0) or (not self._isActivityElement(right[0],vector.getElement(self.__rightName))):
                return False;
        return True;
    
    ##
    # It returns whether the given activity and element are equivalent or not.
    #
    # @param activity: Dinapter's activity to be compared.
    # @param element: Vector element to be compared.
    # @return: Whether the two given arguments are equivalent or not.
    # @defreturn: Boolean  
    def _isActivityElement(self,activity,element):
        toReturn = (self.getType(activity) == element.getType()) and \
               (activity.getDescription() == element.getName()) and \
               (len(activity.getArguments()) == len(element.getData()));
        if not(toReturn):
            return toReturn;
        else:
            boundedPlaceholders = {};
            for i in range(len(element.getData())):
                placeholder = element.getData()[i];
                argument = activity.getArguments()[i];
                if (placeholder in boundedPlaceholders):
                    if boundedPlaceholders[placeholder] != argument:
                        return False;
                else:
                    boundedPlaceholders[placeholder] = argument;
            return True;
            
    
    ##
    # It splits a sequence of activities by the given activity.
    # @param activity: Activity where the sequence must be split.
    # @param sequence: Sequence of activities to split.
    # @return: A tuple containing the split activities.  
    def splitSequence(self,activity,sequence):
        idx = sequence.index(activity);
        previous = sequence[0:idx];
        after = sequence[idx+1:];
        return previous,after;
    
    ##
    # It creates a vector given the left and/or right activities.
    # It automatically creates elements for the activities.
    # @param placeholders: A dictionary of the placeholders to use per activity. 
    # @param left: Left activity or None.
    # @param right: Right activity or None.
    # @return: A vector.
    def createVector(self, placeholders, left=None, right=None):
        if left == right == None:
            raise Exception, "A vector should have an element at least.";
        vector = Vector("v_%u" % self.__counter);
        self.__counter += 1;
        if left != None:
            vector.addElement(self.createElement(placeholders, left, True),self.__leftName);
        if right != None:
            vector.addElement(self.createElement(placeholders, right, False),self.__rightName);
        return vector;
    
    ##
    # It creates a vector element for the given activity.
    # @param placeholders: A dictionary with the lists of placeholders to use per activity.
    # @param activity: Activity the element is going to be created for.
    # @param isLeftSide: True if the activity belongs to the service on the left side. 
    # @return: A vector element. 
    def createElement(self, placeholders, activity, isLeftSide):
        return VectorElement(
                         activity.getDescription()
                        ,self.getType(activity)
                        ,self.__getPlaceholders(placeholders, activity, isLeftSide));
    
    def __getPlaceholders(self, placeholders, activity, isLeftSide):
        global _removedPlaceholders;
        toReturn = list(placeholders.get(activity,[]));
        key = (isLeftSide, activity.getDescription(), self.getType(activity));
        if key in _removedPlaceholders:
            for (pos, placeholder) in _removedPlaceholders[key]:
                toReturn.insert(pos,placeholder);
        return toReturn;
    
    ##
    # It returns the type ("IN" or "OUT") of the given activity.
    # @param activity: Communication activity which type we want.
    # @return: The type ("IN"/"OUT") of the given activity. 
    def getType(self,activity):
        if activity.getType() == self.__BehaviorNodeType.SEND:
            return "OUT";
        elif activity.getType() == self.__BehaviorNodeType.RECEIVE:
            return "IN";
        else:
            raise Exception, "Unknown activity type: "+activity.getNodeType();    
    
    ##
    # Returns a sorted list of dependencies between the given two sequences.
    # The dependencies are a tuples of three elements: the number of dependencies,
    # the first activity, and the second activity.
    # Dependencies are only established between compatible activities (INVOKE-RECEIVE).
    #
    # @param left: Left sequence of activities.
    # @param right: Right sequece of activities.
    # @return: Sorted list of dependencies.   
    def dependencies(self,left,right):
        # Every activity should be INVOKE or RECEIVE.
        matching = [];
        for la in left:
            for ra in right:
                if la.getType() == ra.getType():
                    continue; # Just dependencies between compatible directions/signum.
                matches = 0;
                rags = ra.getArguments();
                for larg in la.getArguments():
                    if larg in rags:
                        matches += 1;
                matching.append((matches,la,ra));
        # @TODO: This is not perfect. Different orders among deps with the same value might have different (better and worse) outcomes.
        matching.sort(lambda x,y: y[0]-x[0]);
        return matching;
    
    ##
    # It creates and returns an vLTS which all the vectors looping on a single initial-end state.
    # @deprecated: These trivial vLTS are superseeded by the system in sequenceVector2lts
    # @return: A "trivial" vLTS with a single state and all the vectors looping on it.
    # @defreturn: vLTS
    def _createTrivialvLTS(self,contract):
        lts = LTS();
        state = State("s0", True, True);
        lts.addState(state);
        lts.addFinal(state.getName());
        lts.setInitial(state.getName());
        for vector in contract.getVectors().keys():
            lts.addTransition(Transition(state.getName(), vector, state.getName()));
            #lts.addTransition(Transition(state.getName(), NLabel(vector), state.getName()));
        return lts;
            
if __name__ == "__main__":
    main();
