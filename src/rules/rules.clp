; ---------------------------------------------------------------------
; This file is part of Dinapter.
;
;  Dinapter is free software; you can redistribute it and/or modify
;  it under the terms of the GNU General Public License as published by
;  the Free Software Foundation; either version 3 of the License, or
;  (at your option) any later version.
;
;  Dinapter is distributed in the hope that it will be useful,
;  but WITHOUT ANY WARRANTY; without even the implied warranty of
;  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;  GNU General Public License for more details.
;
;  You should have received a copy of the GNU General Public License
;  along with this program.  If not, see <http://www.gnu.org/licenses/>.
;
;  (C) Copyright 2007 José Antonio Martín Baena
;  
;  José Antonio Martín Baena <jose.antonio.martin.baena@gmail.com>
;  Ernesto Pimentel Sánchez <ernesto@lcc.uma.es>
; ---------------------------------------------------------------------
;; In order to use customized rules they can be introduced in two ways:
;;	1. In a particular file "rules/rules.clp".
;;	2. Specified in a customized properties file.
;;		- The properties file to load is specified in the system attribute "dinapter.properties".
;;		- Ex: java -Ddinapter.properties=etc/DinapterUnstableProperties.xml -jar Dinapter ...

(provide rules/rules)

;; @todo This should use interfaces only for generecity shake. But Jess doesn't work with Interfaces :(
;(import dinapter.misc.EditDistance)
(import dinapter.specificator.*)
(import dinapter.behavior.*)
(import dinapter.*)
(deftemplate JPowerBehaviorGraph (declare (from-class JPowerBehaviorGraph) (slot-specific TRUE)))
(deftemplate BridgeSpecificatorGraph (declare (from-class BridgeSpecificatorGraph) (slot-specific TRUE)))
(deftemplate MapSpecificatorBuilder (declare (from-class MapSpecificatorBuilder) (slot-specific TRUE)))
(deftemplate SimpleSpecification (declare (from-class SimpleSpecification) (slot-specific TRUE)))
(deftemplate DefaultRule (declare (from-class DefaultRule) (slot-specific TRUE)))
(deftemplate JPowerBehaviorNode (declare (from-class JPowerBehaviorNode) (slot-specific TRUE)))

(deftemplate action-to-add
    "It means that this action may be added to an Specification."
    (slot action (type OBJECT))
    (slot side (type STRING))
    (slot specification (type OBJECT))
    (slot behavior (type OBJECT))
    (multislot leftPath (type OBJECT))
    (multislot rightPath (type OBJECT))
    (multislot pathToAdd (type OBJECT)))
(deftemplate no-fork
    "It avoids any other Specificator graph to be forked. SWITCHs support will be disabled.")
(deftemplate optimistic
    "It makes a best effort to calculate an optimistic heuristic. It will take longer but it's harder to miss the optimal solution.")
(deftemplate cancel-process
    "It means that the process has been canceled manually.")
(deftemplate process-complete
    "It means that the process has ended."
    (slot successfully (type SYMBOL) (default TRUE)))
(deftemplate solution
    "It marks that an Specification is a solution"
    	(declare (slot-specific TRUE))
    	(slot specification (type OBJECT))
    	(slot specificationGraph (type OBJECT))
    	(slot best-solution (type SYMBOL) (default FALSE)))
(deftemplate specifications-to-merge
    "It marks that two specifications can be merged."
    (slot from (type OBJECT))
    (slot to (type OBJECT))
    (slot graph (type OBJECT))
    (slot otherGraph (type OBJECT))
    (multislot rules (type OBJECT) (default (create$))))
(deftemplate relationship
    "It represents a parent-child relationship between to Specificator Graphs. /
    A Specificator graph is child of any graph it has been splitted from. /
    Only 'brother' Sepecificator graphs may be merged."
    (slot parent (type OBJECT))
    (slot child (type OBJECT)))
(deftemplate heritage 
    "It represents an heritage line among Specificator graphs based /
	upon their (relationship)s "
    (declare (ordered TRUE)))
(deftemplate result-tests
    "Whether the tests are going to be performed to check the results of the process")
(deftemplate runtime-tests
    "Whether the tests are allowed during the specification generation.")
(deftemplate actions-required
    "A possible set of actions required to be adapted"
    (multislot actions))
(deftemplate ignore-solution
    "Facts of this template make the rules to ignore the given specification as a solution."
    (slot specification (type OBJECT)))
(deftemplate ignore-current-solutions
    "Ignore and retracts all current solutions")
(deftemplate compatibility
    "How compatible two nodes/actions/activities are"
    (declare (ordered TRUE)))
(deftemplate compatibility-rule
    "Compatibility calculations for a given rule"
    (slot rule (type OBJECT))
    (slot count (type INTEGER))
    (multislot measures (type INTEGER)))

;(watch rules)
;(watch facts)
;(watch activations)

;(assert (optimistic))
;(assert (no-fork))

;; Allows the execution of tests of the generated results.
(assert (result-tests))

;; Allows the execution of tests during the specification generation.
(assert (runtime-tests))

;(batch "rules/debug-rules.clp")

;; CONSTANTS AND GLOBAL VARIABLES ==============================

;; Constant which represents 'no heuristic' value in Specifications.
(defglobal ?*NO_HEURISTIC* = (get-member "SimpleSpecification" "NO_HEURISTIC"))

;; Constant which represents 'no cost' value.
(defglobal ?*NO_COST* = (get-member "SimpleSpecification" "NO_COST"));

;; Constant which represents 'no heuristic' value in Rules.
(defglobal ?*RULE_NO_HEURISTIC* = (get-member "DefaultRule" "NO_HEURISTIC"))

;; This variable eventually holds the count of actions to adapt.
(defglobal ?*ACTIONS_TO_ADAPT* = 0)

;; This variable contains the number of SpecificatorGraphs within Jess.
(defglobal ?*GRAPH_COUNTER* = 0)

;; This variables holds the number of solutions found so far.
(defglobal ?*SOLUTION_COUNTER* = 0)

;; This variable holds the number of Specifications expanded so far.
(defglobal ?*EXPANDED_NODES* = 0)

;; This variable holds the number of existing Specifications.
(defglobal ?*SPECIFICATION_COUNTER* = 0)

;; This variable holds the number of generated rules.
(defglobal ?*RULES_COUNTER* = 0)

;; PENALIZATIONS ===============================================

;; Penalization to apply per each insatisfied argument within a rule.
(defglobal ?*PENALIZATION_PER_INSATISFIED_ARGUMENT* =
    (integer
        (call Dinapter getProperty "PENALIZATION_PER_INSATISFIED_ARGUMENT")))

;; Penalization to apply per each couple of ambiguous rules within an Specification.
(defglobal ?*PENALIZATION_AMBIGUOUS_SPECIFICATION* = 
    (integer
        (call Dinapter getProperty "PENALIZATION_AMBIGUOUS_SPECIFICATION")))
    
;; Penalization to apply if the rule starts with RECEIVEs in both sides.
(defglobal ?*TWO_RECEIVE_PENALIZATION* =
    (integer
        (call Dinapter getProperty "TWO_RECEIVE_PENALIZATION")))
    

;; Penalization to apply if the rule starts with SENDs in both sides.
(defglobal ?*TWO_SEND_PENALIZATION* =
    (integer
        (call Dinapter getProperty "TWO_SEND_PENALIZATION")))

;; Penalization to apply if the rule is empty in one side and starts with RECEIVE in the other.
(defglobal ?*LONELY_RECEIVE_PENALIZATION* =
    (integer
        (call Dinapter getProperty "LONELY_RECEIVE_PENALIZATION")))
    

;; Penalization to apply per each uncompensate action. (Action which
;; doesn't have an action of the other signum (SEND or RECEIVE) in the
;; other side of the rule).
(defglobal ?*SIGNUM_COMPENSATION_PENALIZATION* =
    (integer
        (call Dinapter getProperty "SIGNUM_COMPENSATION_PENALIZATION")))

;; Initial cost to apply to each Specification.
(defglobal ?*INITIAL_COST* = 
    (integer
        (call Dinapter getProperty "INITIAL_COST")))

;; Penalization to apply per each non-adapted action within the Specification.
; ! Very good performance with a value of '2' but it loses solutions.
(defglobal ?*REMAINING_ACTIONS_PENALIZATION* =  
    (integer
        (call Dinapter getProperty "REMAINING_ACTIONS_PENALIZATION")))

;; Whether all the occurrences of an action count for the cost of their specification.
(defglobal ?*COST_ALL_OCCURRENCES_COUNT* = ((call Dinapter getProperty "COST_ALL_OCCURRENCES_COUNT") toLowerCase))

;; Whether every action must be included in the final adaptor specification.
(defglobal ?*ADAPT_EVERY_ACTION* = ((call Dinapter getProperty "ADAPT_EVERY_ACTION") toLowerCase))

;; Penalization which will be multiplied by the compatibility measure [0,100] returned by SimToo.
(defglobal ?*SIM_PENALIZATION* = 
    (float
        (call Dinapter getProperty "SIM_PENALIZATION")))

;; Penalization which will be aplied by default (and later multiplied) to those rules without compatibility measures. 
(defglobal ?*SIM_DEFAULT* = 
    (integer
        (call Dinapter getProperty "SIM_DEFAULT")))

;; QUERIES ======================================================

(defquery query-solution-specifications
    "It finds all solutions found"
    (solution (specification ?specification) 
        (specificationGraph ?graph) 
        (best-solution ?best)))

(defquery query-best-solution-specifications
    "It finds the best solutions found"
    (solution (specification ?specification)
        (best-solution TRUE)))

(defquery query-is-ignored
    "It finds if the given specification has been ignored"
    (declare (variables ?spec))
    (ignore-solution (specification ?spec)))

(defquery query-equivalent-specification
    "It finds an specification with same rules and working rule."
	(declare (variables ?rules ?workingRule))
	(SimpleSpecification (rules ?rules) (workingRule ?workingRule)))

;(defquery query-process-complete
;    "It returns every (process-complete) fact."
;    ?process-complete <- (process-complete))

(defquery query-graphs
    "Return the graph of the given specification"
    (declare (variables ?specification))
    (BridgeSpecificatorGraph
        (allNodes ?nodes&:(?nodes contains ?specification))
        (OBJECT ?graph)))

(defquery query-relation
    "It returns all the relationships with the given child"
    (declare (variables ?child))
    (relationship (parent ?parent) (child ?child)))

(defquery query-heritages
    "It returns the heritages where these specification hashcodes are parent and child"
    (declare (variables ?aHash ?bHash))
    (BridgeSpecificatorGraph (OBJECT ?aGraph&:(eq* ?aHash (?aGraph hashCode))))
    (BridgeSpecificatorGraph (OBJECT ?bGraph&:(eq* ?bHash (?bGraph hashCode))))
    (or
        (heritage $? ?aGraph $? ?bGraph $?)
        (heritage $? ?bGraph $? ?aGraph $?)))

;; FUNCTIONS ====================================================

(deffunction addSpecification
    "Add to rete this specification"
    (?specification)
    (foreach ?rule (?specification getRulesArray)
        (add ?rule))
    (add ?specification))

;; @toreview: Ignoring those specification with no repeated rules exculdes indeterministic behaviours.
;       O       ------O-------
;       |a!     |a?          |a?   
;       O       O            O
(deffunction pushAction
    "Pushes an Action to a side using a MapSpecificatorBuilder. The returned specification can be nil if the new rule was already there."
    (?action ?side ?specification ?BridgeSpecificatorGraph ?builder $?close)
    (?builder setGraph ?BridgeSpecificatorGraph)
    (?builder setWorkingSpecification ?specification)
    (if (and (< 0 (length$ $?close)) (eq TRUE (nth$ 1 $?close))) then
        (?builder closeRule))
    (call ?builder
        (str-cat "push" (upcase (sub-string 1 1 ?side)) (sub-string 2 (str-length ?side) ?side))
        ?action)
    (bind ?new (?builder getWorkingSpecification))
    (if (<= (?new getActionsCount) (?specification getActionsCount)) then
        (?BridgeSpecificatorGraph removeEdge ?specification ?new)
        (return nil)
        else
    	(addSpecification ?new)
    	(return ?new)))

(deffunction splitGraph 
		"Userfunction proxy. It forks the Specificator graph because of /
		 a SWITCH action."
    	(?switch ?action ?side ?specification))

(deffunction mergeGraphs
        "Userfunction proxy. It forks the Specificator graph because of a merge."
    (?parent ?otherParent ?mergedSpecification))

(deffunction getActionsSignumCount
        "It counts difference among the signums of given actions."
    ($?actions)
	(bind ?send 0)
    (bind ?receive 0)
	(foreach ?action $?actions
    	(if (eq (?action getNodeType) "SEND") then
            (++ ?send)
         else (if (eq (?action getNodeType) "RECEIVE") then
            (++ ?receive))))
	(return (create$ ?send ?receive)))

(deffunction assertActionToAdd
        "It asserts (action-to-add) with given arguments plus /
		adding $?pathToAdd to the path of the parent specification."
    (?action ?side ?specification ?behavior $?pathToAdd)
    (bind $?newLeftPath (?specification getLeftPath))
    (bind $?newRightPath (?specification getRightPath))
	(if (eq ?side "left") then
        (bind $?newLeftPath (create$ $?newLeftPath $?pathToAdd))
    else
        (bind $?newRightPath (create$ $?newRightPath $?pathToAdd)))
	(assert 
		(action-to-add 
				(action ?action) 
				(side ?side) 
				(specification ?specification) 
				(behavior ?behavior) 
				(leftPath $?newLeftPath)
				(rightPath $?newRightPath)
            	(pathToAdd $?pathToAdd))))

(deffunction pathToString
        "It translates a path to a single String."
    ($?path)
    (if (= 0 (length$ $?path)) then
        (return "")
    else
    	(return (str-cat ((nth$ 1 $?path) toString) "; " (pathToString (rest$ $?path))))))

(deffunction log
    	"Userfunction proxy. It logs a message."
    (?type $?arguments))

(deffunction cancel
    	"It cancels the process"
    ()
    (assert (cancel-process)))

(deffunction closedRulesHeuristic
    	"Returns the heuristic values of all the closed rules."
    (?specification $?rules)
    (bind ?ac 0)
    (foreach ?rule $?rules
        (if (neq (?specification getWorkingRule) ?rule) then
            (bind ?ac (+ ?ac (?rule getHeuristic)))))
    (return ?ac))

(deffunction testFailed
    	"Function called when a test fails."
    (?test ?message ?argument)
    (log error "Test failed: " ?test " -- " ?message " " ?argument)
    (return nil))

;; A) Create specification children ==============================

;; @todo This may not work properly within loops.
(defrule add-switch-children
        "Detected a switch. It will be processed after all other nodes"
    (not (no-fork))
    (JPowerBehaviorNode (nodeType "IF") (OBJECT ?switchAction))
    ?f <- (action-to-add (action ?switchAction) (side ?side) (specification ?specification) (behavior ?behavior))
    (SimpleSpecification (OBJECT ?specification) (childrenNeeded TRUE))
    (BridgeSpecificatorGraph 
        (allNodes ?nodes&:(?nodes contains ?specification)) ; This kind of CE, despite it's generally discouraged, works because of Specifications nature and adding procedure.
    	(OBJECT ?specificationGraph))
    =>
    (retract ?f)
    (printout t "SWITCH: " ((?behavior getChildren ?switchAction) size) " new graphs." crlf)
    (foreach ?child ((?behavior getChildren ?switchAction) toArray)
        (bind ?newGraph (splitGraph ?switchAction ?child ?side ?specification))
        (if (neq ?newGraph nil) then
			(assert 
	        	(relationship 
	                (parent ?specificationGraph) 
	        		(child ?newGraph))))))

(defrule add-direct-adaptative-child-adding
    "If we have some adaptative actions to be added we do so now continuing with the working rule"
    ?f <- (action-to-add (action ?action) (side ?side) (specification ?specification) 
        				(leftPath $?leftPath) (rightPath $?rightPath))
    (SimpleSpecification (OBJECT ?specification) (childrenNeeded TRUE) (merged FALSE))
    (JPowerBehaviorNode (OBJECT ?action) (nodeType "SEND"|"RECEIVE"))
    (BridgeSpecificatorGraph (allNodes ?nodes&:(?nodes contains ?specification)) (OBJECT ?graph))
    (MapSpecificatorBuilder (OBJECT ?builder))
    =>
    ;(retract ?f) ;; It will be retracted in retract-add-child.
    (bind ?created (pushAction ?action ?side ?specification ?graph ?builder))
    ;; These two following sentences are needed because of (ignore-pick-assert). Otherwise there won't be PICKs in the PATHS
    (if (neq ?created nil) then
    	(?created setLeftPath $?leftPath)
    	(?created setRightPath $?rightPath)))

(defrule add-direct-adaptative-child-closing
    "If we have some adaptative actions to be added we do so now but closing previous rule"
    ?f <- (action-to-add (action ?action) (side ?side) (specification ?specification) 
        				(leftPath $?leftPath) (rightPath $?rightPath))
    (SimpleSpecification (OBJECT ?specification) (childrenNeeded TRUE) (workingRule ~nil))
    (JPowerBehaviorNode (OBJECT ?action) (nodeType "SEND"|"RECEIVE"))
    (BridgeSpecificatorGraph (allNodes ?nodes&:(?nodes contains ?specification)) (OBJECT ?graph))
    (MapSpecificatorBuilder (OBJECT ?builder))
    =>
    ;(retract ?f) ;; It will be retracted in retract-add-child.
    (bind ?created (pushAction ?action ?side ?specification ?graph ?builder TRUE))
    ;; These two following sentences are needed because of (ignore-pick-assert). Otherwise there won't be PICKs in the PATHS
    (if (neq ?created nil) then
	    (?created setLeftPath $?leftPath)
	    (?created setRightPath $?rightPath)))

(defrule retract-add-child
    "It will retract already processed (action-to-add) asserts."
    (declare (salience -10))
    ?f <- (action-to-add)
    =>
    (retract ?f))
    
(defrule assert-child-to-add
    "Assert an Action to add to a Specification"
	(SimpleSpecification (childrenNeeded TRUE) (OBJECT ?specification) (leftPath $?leftPath) (rightPath $?rightPath))
    (JPowerBehaviorGraph 
    		(startNode ?startNode) 
			(OBJECT ?behavior)
        	(allNodes ?nodes)
			(side ?side))
    (JPowerBehaviorNode 
        (OBJECT ?action&:(?nodes contains ?action)))
    (or
        (JPowerBehaviorNode (OBJECT ?parent
                    &:(?nodes contains ?parent)
                    &:((?behavior getChildren ?parent) contains ?action)
            		&:(or
                        (eq ?parent (nth$ (length$ $?leftPath) $?leftPath)) 
                		(eq ?parent (nth$ (length$ $?rightPath) $?rightPath)))))
        (test (and
                ((?behavior getChildren ?startNode) contains ?action)
				(not (member$ ?startNode $?leftPath))
            	(not (member$ ?startNode $?rightPath)))))
	=>
    (if ((?behavior getChildren ?startNode) contains ?action) then
        (bind $?toAdd (create$ ?startNode ?action))
    else
        (bind $?toAdd ?action))
	(assertActionToAdd ?action ?side ?specification ?behavior $?toAdd))
	
(defrule ignore-pick-assert
    "We ignore picks and assert them like direct adaptative children"
	(JPowerBehaviorNode (OBJECT ?action) (nodeType "PICK"))
    (action-to-add (action ?action) (side ?side) (specification ?specification) (behavior ?behavior) (pathToAdd $?pathToAdd))
    (SimpleSpecification (childrenNeeded TRUE) (OBJECT ?specification))
	(JPowerBehaviorNode (OBJECT ?child
            				&:((?behavior getChildren ?action) contains ?child)))
	=>
    (assertActionToAdd ?child ?side ?specification ?behavior $?pathToAdd ?child))

(defrule ignore-pick-retract
    "Retracts (action-to-add) of PICK if it has been fully proccessed"
	?f <- (action-to-add (action ?action) (specification ?specification))
    (SimpleSpecification (OBJECT ?specification) (childrenNeeded FALSE))
	(JPowerBehaviorNode (OBJECT ?action) (nodeType "PICK"))
	=>
	(retract ?f))

;; @toreview How does herency interact with cut specifications?
;; @toreview Cut specifications could be retracted and removed from the graphs but they are a minority.
(defrule cut-equivalent-specifications-1
    "It removes all equivalent specifications"
    (declare (salience 70)) ; Before any Specification children creation.
	(SimpleSpecification ; @toreview This would be a good place for an (exists) but it doesn't work. I don't know why.
        (childrenReady TRUE) ; In order to reduce activations of this rule.
        (copiedSpecification FALSE) ; No conflict with copied specifications.
        (cuttedSpecification FALSE)
        (rules ?rules)
		(workingRule ?wRule)
    	(OBJECT ?other))
    ?fact <- (SimpleSpecification
        (childrenReady FALSE)
		(cuttedSpecification FALSE) ; Why to do again what has been done?
        (copiedSpecification FALSE) ; No conflict with copied specifications.
		(childrenNeeded ?) ; !! We cannot modify childrenNeeded FALSE nodes because of syncrhonization issues.
		(rules ?rules)
		(workingRule ?wRule) 
		(OBJECT ?another&~?other)) 
	=>
    (printout t "EQUIVALENT CUT: An specification was cut." crlf)
    (modify ?fact (cuttedSpecification TRUE))
    (log "debug" "An specification was cut." (?another toString)))

(defrule cut-equivalent-specifications-2
    "It actually cuts equivalent specifications. This rule assures that the
    Specification will eventually be cut even though other threads change these
    values."
	(declare (salience 40))
    (or
		?f <- (SimpleSpecification (cuttedSpecification TRUE) (childrenNeeded TRUE))
        ?f <- (SimpleSpecification (cuttedSpecification TRUE) (childrenReady FALSE)))
	=>
	(modify ?f (childrenNeeded FALSE) (childrenReady TRUE)))

(defrule merge-switches
    "It merges Specification generated by complementative switch branches"
    ; @tofix We must force that every branch generated by the same switch get merged.
    ; @todo The eficiency greatly relies on how many times this rule gets fired.
    (BridgeSpecificatorGraph (OBJECT ?aGraph))
    (BridgeSpecificatorGraph 
        (OBJECT ?otherGraph
            ; @toreview Order DOES matter because of heuristic and costs calculations...
    		&:(<= (?otherGraph hashCode) (?aGraph hashCode)) ; Doesn't redo in the oposite order.
            &~?aGraph))
    (not
        (or
        	(heritage $? ?aGraph $? ?otherGraph $?)
    		(heritage $? ?otherGraph $? ?aGraph $?)))
	(SimpleSpecification 
        (OBJECT ?aSpec&:(?aGraph containsNode ?aSpec)) (childrenReady TRUE) (copiedSpecification FALSE) 
		(cuttedSpecification FALSE) (actions $?aActions) (leftPath $? ?lastLeft) (rightPath $? ?lastRight)
		(workingRule ?wRA))
	(SimpleSpecification 
        (leftPath $? ?lastLeft) (rightPath $? ?lastRight) (childrenReady TRUE) (cuttedSpecification FALSE) 
        (copiedSpecification FALSE) (rules ?rule&~:(?rule isEmpty)) (actions $?bActions)
    		(OBJECT ?otherSpec&~?aSpec&:(?otherGraph containsNode ?otherSpec)) (workingRule ?wRB))
    ;; The previous action to the last in the path must be different between the merges.
    ;; @todo This fails because of the SWITCH and PICK behavior.
    ;; SWITCH and PICK are included in a special way in the PATH and that is an exception
    ;; that makes this fail in some example like FTP-small where it returns a repeated ?get(filename).
    ;(or 
    ;    (SimpleSpecification (OBJECT ?aSpec) (leftPath ?lastLeft))
    ;    (SimpleSpecification (OBJECT ?otherSpec) (leftPath ?lastLeft))
    ;    (SimpleSpecification (OBJECT ?aSpec) (rightPath ?lastRight))
    ;    (SimpleSpecification (OBJECT ?otherSpec) (rightPath ?lastRight))
    ;    (and
    ;    	(SimpleSpecification (OBJECT ?aSpec) (leftPath $? ?previous ?lastLeft))
    ;       (SimpleSpecification (OBJECT ?otherSpec) (leftPath $? ~?previous ?lastLeft)))
    ;    (and
    ;    	(SimpleSpecification (OBJECT ?aSpec) (rightPath $? ?previous ?lastRight))
    ;       (SimpleSpecification (OBJECT ?otherSpec) (rightPath $? ~?previous ?lastRight))))
    ;; Check that, merging, we obtain new actions required in the solution.        	
	(test
        ;(and
	    	(not        
	        	(or
	                (subsetp $?bActions $?aActions)
	            	(subsetp $?aActions $?bActions)))
            ;(or ; This would cut out duplicated merges but it will be performed in addSearch
            ;    (neq ?wRA ?wRB)
		)            
    (not (process-complete)) ; No further merges after completition.
	=>
    ;(printout t "MERGE: Stage 1" crlf)
    ;(printout t "%%%%%%%%%%%%% Merging" crlf (?aSpec toString) crlf "-----------------" crlf (?otherSpec toString) crlf)
    (assert (specifications-to-merge (to ?aSpec) (from ?otherSpec) (graph ?aGraph) (otherGraph ?otherGraph))))

(defrule merge-specification-rules
    "It merges the rules from two Specifications"
	?f <- (specifications-to-merge (to ?to) (from ?from) (graph ?graph) (rules $?rulesToAdd))
	(SimpleSpecification (OBJECT ?from) (rulesArray $? ?rule&~:(member$ ?rule $?rulesToAdd) $?))
	(SimpleSpecification 
        (OBJECT ?to)
        (rules ?rules&~:(?rules contains ?rule)))
    =>
    ;(printout t "MERGE: Stage 2" crlf)
    (modify ?f (rules (create$ ?rule $?rulesToAdd))))

(defrule retract-empty-merges
    "If there're no rules to merge we retract the fact"
    (declare (salience -4)) ; After (merge-specification-rules) and before (merge-specifications)
	?f <- (specifications-to-merge (rules))
	=>
    ;(printout t "MERGE: Cancelled, no rules." crlf)
	(retract ?f))

(defrule retract-equivalent-merges
    "If there is already an equivalente merge we retract the fact"
	(declare (salience -4)) ; After (merge-specification-rules) and before (merge-specifications)
	?f <- (specifications-to-merge (to ?to) (rules $?newRules))
    (SimpleSpecification (OBJECT ?to) (rules ?oldRules))
    (SimpleSpecification
    	(rules ?rules
            	&:(?rules containsAll ?oldRules)
    			&:(?rules containsAll (call java.util.Arrays asList $?newRules))))
    =>
    ;(printout t "MERGE: Cancelled, equivalent one already exists")
    (retract ?f))

;; @todo Merge paths smarter than just copying.
(defrule merge-specifications
    "It finally merges two Specifications"
    (declare (salience -5)) ; After (merge-specification-rules) and before Specifcation costs calculations
    ?f <- (specifications-to-merge (to ?to) (from ?from) (graph ?graph) (otherGraph ?otherGraph) (rules $?rules))
    (SimpleSpecification (OBJECT ?to) (rulesArray $?oldRules) (leftPath $?leftPath) (rightPath $?rightPath) (workingRule ?wR))
    (SimpleSpecification (OBJECT ?from) (leftPath $?otherLeftPath) (rightPath $?otherRightPath))
	(MapSpecificatorBuilder (OBJECT ?builder))
	=>
    (retract ?f)
    (bind ?newSpec (?builder createSpecification (create$ $?rules $?oldRules)))
    (set ?newSpec workingRule ?wR) ; The rulesArray is not sorted so we cannot assume the working rule.
    (?newSpec setMerged TRUE)
    (?newSpec setLeftPath $?leftPath) (?newSpec setRightPath $?rightPath)
    ;; --- Aditional Paths --
    (?newSpec addAdditionalPaths ?to) ; This line and the following copies its parrent additional paths.
    (?newSpec addAdditionalPaths ?from)
    (?newSpec addLeftPath $?otherLeftPath) (?newSpec addRightPath $?otherRightPath) ; The other path will be registered.
    ;; ======================
    (printout t "MERGE: Two Specifications." crlf)
    ;(printout t "%%%%%%%%%%%%% Merging" crlf (?to toString) crlf "-----------------" crlf (?from toString) crlf "_______ INTO _______" crlf (?newSpec toString) crlf "====================" crlf)
	(bind ?child (mergeGraphs ?to ?from ?newSpec))
    (assert (relationship (parent ?graph) (child ?child)))
    (assert (relationship (parent ?otherGraph) (child ?child))))
    
;; This breaks omitted PICKs
(defrule add-end-to-paths
    "It adds the END BehaviorNode to Specifications paths when possible"
    (JPowerBehaviorNode (OBJECT ?end) (nodeType "EXIT"))
    (JPowerBehaviorGraph 
        (nodes ?nodes&:(?nodes contains ?end))
    	(OBJECT ?graph)
    	(side ?side))
    (JPowerBehaviorNode (OBJECT ?lastAction
            &:(?nodes contains ?lastAction)
    		&:((?graph getChildren ?lastAction) contains ?end)))
    (or
		?f <- (SimpleSpecification (leftPath $?path ?lastAction))
    	?f <- (SimpleSpecification (rightPath $?path ?lastAction)))
	=>
    (if (eq ?side "left") then
        (modify ?f (leftPath $?path ?lastAction ?end))
    else
    	(modify ?f (rightPath $?path ?lastAction ?end))))

;; A1) Parents, children and brothers ==========================

(defrule new-heritage
        "It creates a new heritage line among Graphs"
    (declare (salience 40))
	(relationship (parent ?parent) (child ?child))
	(not (heritage $? ?parent $?))
	(not (relationship (child ?parent)))
	=>
	(assert (heritage ?parent ?child)))

(defrule continue-heritage
        "It appends a Graph to an already existing heritage line"
    (declare (salience 50))
	(relationship (parent ?parent) (child ?child))
	?h <- (heritage $?line&:(eq ?parent (nth$ (length$ $?line) $?line)))
	=>
	(retract ?h)
    (assert (heritage (create$ $?line ?child))))

(defrule split-heritage
        "If a Graph must be included in the middle of an heritage line, this line gets splitted."
    (declare (salience 50))
	(relationship (parent ?parent) (child ?child))
	(heritage $?before ?parent ? $?)
	(not (heritage $?before ?parent $? ?child $?))
	=>
	(assert (heritage (create$ $?before ?parent ?child))))
	
;; B) Set heuristic and costs ==================================

;; -- Heuristic and cost initialization --

(defrule initialization-zero-heuristic
    "Initialize to zero all rules and specification heuristics"
    (declare (salience 50))
    (or 
        ?fact <- (DefaultRule (heuristic ?*RULE_NO_HEURISTIC*))
        ?fact <- (SimpleSpecification (heuristic ?*NO_HEURISTIC*) (OBJECT ?specification) (copiedSpecification FALSE)))
    =>
    (modify ?fact (heuristic 0)))

(defrule initialization-cost
    "Initialize all specification costs"
    (declare (salience 50))
    ?specification <- (SimpleSpecification (cost ?*NO_COST*) (OBJECT ?specificationObject) (copiedSpecification FALSE))
    =>
    (modify ?specification (cost ?*INITIAL_COST*))
	(?specificationObject log (str-cat "- Initialized to cost=" ?*INITIAL_COST*)))

;; -- Heuristic and cost rules --
;; - Rules

(defrule rule-has-same-start-signum-penalization
        "It penalizes rules witch start with actions of the same signum in both sides."
    ?r <- (DefaultRule 
    		(OBJECT ?rule)
			(leftSideArray ?action $?) (rightSideArray ?other $?))
	(JPowerBehaviorNode (OBJECT ?action) (nodeType ?type))
	(JPowerBehaviorNode (OBJECT ?other) (nodeType ?type))
	=>
	(if (eq ?type "RECEIVE") then
    		(bind ?penalization ?*TWO_RECEIVE_PENALIZATION*)
	else
		(bind ?penalization ?*TWO_SEND_PENALIZATION*))
	(modify ?r (heuristic (+ (?rule getHeuristic) ?penalization))))

(defrule rule-arguments-insatisified-penalization
   		;; @todo This rule reutilizes same argument several times :(. It's not bad but tricky.
    	"Rules that don't use all their action arguments in within have bigger heuristic"
    (JPowerBehaviorNode (OBJECT ?action) (argumentsArray $? ?argument $?))
    (or
    	?rule <- (DefaultRule (leftSideArray $? ?action $?) (rightSideArray $?otherActions) (OBJECT ?ruleObject))
       	?rule <- (DefaultRule (leftSideArray $?otherActions) (rightSideArray $? ?action $?) (OBJECT ?ruleObject)))
    (not
        (JPowerBehaviorNode 
            (OBJECT ?otherAction&:(member$ ?otherAction $?otherActions)) 
            (argumentsArray $? ?argument $?)))
    =>
    (modify ?rule 
        (heuristic 
            (+ 
                (?ruleObject getHeuristic)
                ?*PENALIZATION_PER_INSATISFIED_ARGUMENT*))))

(defrule rule-signum-compensation
    "It penalizes rules with uncompensated actions signums"
	?ruleFacts <- (DefaultRule ;; !! Rules must be modified using (modify)
        (OBJECT ?rule) 
    	(leftSideArray $?left) 
    	(rightSideArray $?right))
	=>
	(bind $?lc (getActionsSignumCount $?left))
	(bind $?rc (getActionsSignumCount $?right))
	(modify ?ruleFacts (heuristic (+ 
        	(?rule getHeuristic)
		(* ?*SIGNUM_COMPENSATION_PENALIZATION* (+
        		(abs (- (nth$ 1 $?lc) (nth$ 2 $?rc)))
			(abs (- (nth$ 2 $?lc) (nth$ 1 $?rc)))))))))

(defrule rule-has-lonely-receive
    "The rule has an empty side an a receive starting the other"
    (JPowerBehaviorNode (OBJECT ?receive) (nodeType "RECEIVE"))
    (or
	    ?r <- (DefaultRule ;; !! Rules must be modified using (modify)
	        	(OBJECT  ?rule)
	        	(leftSideArray) (rightSideArray ?receive $?))
        ?r <- (DefaultRule ;; !! Rules must be modified using (modify)
	        	(OBJECT  ?rule)
	        	(leftSideArray ?receive $?) (rightSideArray)))
    =>
    (modify ?r (heuristic (+ (?rule getHeuristic) ?*LONELY_RECEIVE_PENALIZATION*))))

(defrule rule-action-sim-create
    "Two actions within a rule has a compatibility measure so lets create the temporal data"
    (declare (salience 5)) ; Before the heuristic modification (0)
    (test (not (= 0 ?*SIM_PENALIZATION*)))
    (DefaultRule (OBJECT ?rule) (leftSideArray $? ?a $?) (rightSideArray $? ?b $?))
    (compatibility ?a ?b ?comp)
    =>
    ;(printout t "A compatibility found for a rule" crlf)
    (assert (compatibility-rule (rule ?rule) (count 1) (measures ?comp))))
    
(defrule rule-action-sim-default
    "Default penalization where no compatibility measure is available"
    (declare (salience 5))
    (test (not (= 0 ?*SIM_PENALIZATION*)))
    (test (not (= 100 ?*SIM_DEFAULT*)))
    (DefaultRule (OBJECT ?rule) (leftSideArray $?left) (rightSideArray $?right))
    (not (compatibility ?a&:(member$ ?a $?left) ?b&:(member$ ?b $?right) ?))
    =>
    ;(printout t "Default compatibility for a rule" crlf)
    (assert (compatibility-rule (rule ?rule) (count 1) (measures ?*SIM_DEFAULT*))))
    
 (defrule rule-action-sim-recount
    "Two actions within a rule has a compatibility measure so we update the temporal data"
    (declare (salience 5)) ; Before the heuristic modification (0)
    (test (not (= 0 ?*SIM_PENALIZATION*)))
    ?f1 <- (compatibility-rule (rule ?rule) (count ?c1) (measures $?m1))
    ?f2 <- (compatibility-rule (rule ?rule) (count ?c2) (measures $?m2))
    (test (neq ?f1 ?f2))    
    =>
    ;(printout t "Two compatibilities recounted into a new one" crlf)
    (retract ?f1)
    (retract ?f2)
    (assert (compatibility-rule (rule ?rule) (count (+ ?c1 ?c2)) (measures (insert$ $?m1 1 $?m2)))))    
    
(defrule rule-action-sim-modify
    "It modifies the heuristic of a rule by its compatibility measure average"
    (test (not (= 0 ?*SIM_PENALIZATION*)))
    ?f <- (compatibility-rule (rule ?rule) (count ?c) (measures $?m))
    ?r <- (DefaultRule (OBJECT ?rule))
    =>
    ;(printout t "Updating the heuristic of a rule by its compatibility" crlf)
    (retract ?f)
    (bind ?v 0)
    (foreach ?x $?m (bind ?v (+ ?v ?x)))
    (bind ?result (integer (* ?*SIM_PENALIZATION* (- 100 (/ ?v ?c)))))
    ;(printout t "Similarity " (?rule toString) " = " ?result crlf)
    (modify ?r (heuristic (+ (?rule getHeuristic) ?result))))

;(defrule rule-edit-distance
;    "It penalizes the rule with the sum of the minimum edit distance among the action names"
;    ?r <- (DefaultRule ;; !! Rules must be modified using (modify)
;        (OBJECT ?rule)
;        (leftSideArray $?lefts)
;        (rightSideArray $?rights))
;    =>
;    (bind ?totalDistance 0)
;    (if (= 0 (length$ $?lefts))
;        then
;            (bind $?lefts (list ""))
;        else
;            (bind $?lefts (map (lambda (?action) (?action getDescription)) $?lefts)))
;    (bind $?rights (map (lambda (?action) (?action getDescription)) $?rights))
;    (foreach ?left  $?lefts
;        (bind ?distance 9999)
;        (foreach ?right $?rights
;            (bind ?current (call EditDistance getEditDistance ?left ?right))
;            (if (< ?current ?distance) then (bind ?distance ?current)))
;        (if (= 9999 ?distance) then 
;            (bind ?distance (call EditDistance getEditDistance ?left "")))
;        (bind ?totalDistance (+ ?totalDistance ?distance)))
;    (modify ?r (heuristic (+ (?rule getHeuristic) ?totalDistance))))

;; - Specifications
    
(defrule specification-has-recently-closed-rule-cost
    "When a rule gets closed it heuristic is added to its Specification cost. Only one closed rule per parent."
    (declare (salience -10)) ; All rules heuristics must be calculated before.
    (or
    	(DefaultRule (OBJECT ?workingRule) (leftSideArray) (rightSideArray ?))
		(DefaultRule (OBJECT ?workingRule) (leftSideArray ?) (rightSideArray)))
    ?specification <- (SimpleSpecification (rulesArray $? ?rule $?) (workingRule ?workingRule&~?rule)
        (OBJECT ?specificationObject) (copiedSpecification FALSE))
    (BridgeSpecificatorGraph (allNodes ?nodes&:(?nodes contains ?specificationObject)) (OBJECT ?graph))
    ;; @toreview Por qué lo siguiente? - Es para los nodos mezclados. Desfasado.
    ;(not (SimpleSpecification (workingRule ?workingRule) (OBJECT ?parent
    ;            &:(?nodes contains ?parent)
    ;            &:((?graph getChildren ?parent) contains ?specificationObject))))
    (SimpleSpecification (workingRule ?rule) (cuttedSpecification FALSE) (OBJECT ?parent
                &:(?nodes contains ?parent)
                &:((?graph getChildren ?parent) contains ?specificationObject)))
		
    (DefaultRule (heuristic ?heuristic) (OBJECT ?rule))
    =>
   	(?specificationObject log (str-cat "- Recently closed rule: " (?specificationObject getCost) " + " ?heuristic))
    (modify ?specification (cost (+ (?specificationObject getCost) ?heuristic))))

(defrule specification-has-working-rule-heuristic
    "Specification herusitic depends on its working rule heuristic"
    (declare (salience -10)) ; All rules heuristics must be calculated before.
    (not (optimistic))
    ;    (SimpleSpecification (OBJECT ?specificationObject) (solution TRUE))) This doesn't work properly
    ?specification <- (SimpleSpecification (workingRule ?workingRule) (OBJECT ?specificationObject) (copiedSpecification FALSE))
    (DefaultRule (heuristic ?heuristic) (OBJECT ?workingRule))
    =>
    (?specificationObject log (str-cat "- Working rule heurisitc: " (?specificationObject getHeuristic) " + " ?heuristic))
    (modify ?specification (heuristic (+ (?specificationObject getHeuristic) ?heuristic))))

(defrule specification-has-remaining-adaptative-actions-heuristic
    "Add remaining adaptative actions to the specification heuristic"
	(SimpleSpecification (OBJECT ?specification) (copiedSpecification FALSE) (actions $?actions))
    =>
    (bind ?toAdd (- ?*ACTIONS_TO_ADAPT* (length$ $?actions)))
    (?specification log (str-cat "- Remaining actions plus: " (?specification getHeuristic) " + " ?toAdd))
    (set ?specification "heuristic" (* ?*REMAINING_ACTIONS_PENALIZATION* (+ 
                (?specification getHeuristic) ?toAdd))))

; @tofix @todo This should be added to cost. It never gets sum into the cost otherwise and it's already an stablished fact.
(defrule specification-is-ambiguous
    "We penalize a Specification with ambiguous rules. An ambiguos specification contains two rules 
    which starts in one side with the same SEND action and those rule don't have a different SEND actions
    to distinguish them in the other side."
    (JPowerBehaviorNode (OBJECT ?send) (nodeType "SEND"))
	(or
    	(and
    		(DefaultRule (OBJECT ?aRule) (leftSideArray ?send $?) (rightSideArray $?aOther))
			(DefaultRule (OBJECT ?bRule&~?aRule) (leftSideArray ?send $?) (rightSideArray $?bOther)))
		(and
    		(DefaultRule (OBJECT ?aRule) (leftSideArray $?aOther) (rightSideArray ?send $?))
			(DefaultRule (OBJECT ?bRule&~?aRule) (leftSideArray $?bOther) (rightSideArray ?send $?))))
	(or
		(test
	        (or
	        	(= 0 (length$ $?aOther))
	    		(= 0 (length$ $?bOther))
	    		(eq (first$ $?aOther) (first$ $?bOther))))
		(not
	        (or
	        	(JPowerBehaviorNode (OBJECT =(nth$ 1 $?aOther)) (nodeType "SEND"))
	    		(JPowerBehaviorNode (OBJECT =(nth$ 1 $?bOther)) (nodeType "SEND")))))
	?specification <- (SimpleSpecification
        (OBJECT ?specificationObject)
    	(rules ?rules&:(?rules contains ?aRule)&:(?rules contains ?bRule))
    	(copiedSpecification FALSE))
	=>
    (?specificationObject log (str-cat " - Penalized for ambiguous rules: " 
                (?specificationObject getHeuristic) 
        		" -> " 
        		?*PENALIZATION_AMBIGUOUS_SPECIFICATION*))
	(modify ?specification (heuristic (+ (?specificationObject getHeuristic) 
            ?*PENALIZATION_AMBIGUOUS_SPECIFICATION*))))

;; C) Children ready ========================================

(defrule specification-children-ready
    "A specification has all its children already calculated"
    (declare (salience -100))
    ?specification <- (SimpleSpecification (childrenNeeded TRUE) (OBJECT ?specificationInstance))
    (not (cancel-process))
    =>
    (modify ?specification (childrenNeeded FALSE) (childrenReady TRUE))
	(log "trace" "------------- Specification Ready: " crlf ?specificationInstance))

(defrule specification-cost-ready
        "The cost and the heuristic of this Specification have been calculated already."
    (declare (salience -15))
	?f <- (SimpleSpecification (costReady FALSE) (OBJECT ?specificationObject) (rules ?rules))
    ;; The following is just to make sure that (specification-has-recently-closed-rule-cost) is fired if needed.
    (BridgeSpecificatorGraph (OBJECT ?graph) (allNodes ?nodes&:(?nodes contains ?specificationObject)))
    ;(or ; The rest doesn't seem necesary, just checking the graph this seems to wait for (specification-has-recently-closed-rule-cost)
    ;    (test (?rules isEmpty))
    ;    (SimpleSpecification (OBJECT ?parent&:(?nodes contains ?parent)&:((?graph getChildren ?parent) contains ?specificationObject))))
	=>
    (?specificationObject log "- Cost Ready")
    (modify ?f (costReady TRUE)))

;; D) Tests =================================================

; @todo Implement Specification integrity tests.

(defrule test-specification-without-graph
        "It tests that there are no Specification without Graph."
    (process-complete)
    (result-tests)
    (SimpleSpecification (OBJECT ?spec))
	(not (BridgeSpecificatorGraph (OBJECT ?graph&:(?graph containsNode ?spec))))
	=>
	(testFailed "test-specification-without-graph"
        						   "Specification without graph found"
								   (?spec toString)))

(defrule test-specifications-are-ready
        "If the process is complete, all the Specifications must be Ready."
    (process-complete (successfully TRUE))
    (result-tests)
    (SimpleSpecification (childrenReady FALSE) (OBJECT ?specification))
    (not (solution))
	=>
	(testFailed
        "test-specifications-are-ready" 
		"There are no solutions but the following specification is not explored yet (not children ready)" (?specification toString)))

(defrule test-specifications-aren't-needed
        "If the process is complete, no Specification may need more children."
    (process-complete (successfully TRUE))
    (result-tests)
    (SimpleSpecification (childrenNeeded TRUE) (OBJECT ?specification))
	=>
	(testFailed
        "test-specifications-aren't-needed" 
		"Following Specification still needs some children" (?specification toString)))

(defrule test-cutted-specifications-are-childless
        "If the process is complete no cut Specification may have any children."
    (process-complete (successfully TRUE))
    (result-tests)
    (SimpleSpecification (cuttedSpecification TRUE) (OBJECT ?specification))
    ;; We allows cutted parent specifications for merged specifications.
    (SimpleSpecification (merged FALSE) (OBJECT ?child))
    (BridgeSpecificatorGraph
        (OBJECT ?graph&:(?graph containsNode ?specification)&:((?graph getChildren ?specification) contains ?child)))
	=>
    (testFailed
            "test-cutted-specifications-are-childless"
            "Following cutted specification does have children"
    		(?specification toString)))

(defrule test-equivalent-specifications
        "It test that there are no Graphs with equivalent Specifications within."
    (process-complete (successfully TRUE))
    (result-tests)
    ;; We allow several instances of empty specifications in the same graph.
	(SimpleSpecification (rules ?rules) (workingRule ?wR&~nil) (OBJECT ?specification))
	(SimpleSpecification (rules ?rules) (workingRule ?wR) (OBJECT ?other&~?specification))
	(BridgeSpecificatorGraph
        (OBJECT ?graph&:(?graph containsNode ?specification)&:(?graph containsNode ?other)))
    	=>
	(testFailed
        	"test-equivalent-specifications"
			"Equivalent specifications found in same graph"
			(str-cat (?specification toString) (?other toString))))

(defrule test-children-allowed
        "Pre-test. It test that the only behavior nodes with several children are SWITCHs and PICKs."
    (JPowerBehaviorNode (OBJECT ?aNode) (nodeType ~"IF"&~"PICK"))
	(JPowerBehaviorGraph
    	(nodes ?nodes&:(?nodes contains ?aNode))
		(OBJECT ?graph&:(< 1 ((?graph getChildren ?aNode) size))))
	=>
    (testFailed
        	"test-chilren-allowed"
			"A normal action has several children"
			(?aNode toString)))

(defrule test-pick-has-receives
    "It tests that a PICK node has all its children of RECEIVE type"
	(JPowerBehaviorNode (OBJECT ?aNode) (nodeType "PICK"))
	(JPowerBehaviorNode (OBJECT ?child) (nodeType ~"RECEIVE"))
	(JPowerBehaviorGraph 
    	(nodes ?nodes&:(?nodes contains ?aNode)&:(?nodes contains ?child))
		(OBJECT ?graph&:((?graph getChildren ?aNode) contains ?child)))
	=>
	(testFailed
        	"test-pick-has-receives"
			"A PICK action must only have RECEIVE children"
			(?aNode toString)))

(defrule test-switch-has-send
    "It tests that all SWITCHes have only SEND children"
	(JPowerBehaviorNode (OBJECT ?aNode) (nodeType "IF"))
	(JPowerBehaviorNode (OBJECT ?child) (nodeType ~"SEND"))
	(JPowerBehaviorGraph 
		(OBJECT ?graph&:(?graph containsNode ?aNode)&:(?graph containsNode ?child)&:((?graph getChildren ?aNode) contains ?child)))
	=>
	(testFailed
        	"test-switch-has-send"
			"A SWITCH action must only have SEND children"
			(str-cat "(" (?aNode toString) "->" (?child toString) ")")))

(defrule test-no-acumulated-cost
    "Test if there're ready Specifications without the acumulated cost calculated"
    (process-complete (successfully TRUE)) ; We are finished
    (result-tests)
	(SimpleSpecification (OBJECT ?spec) 
        				 (acumulatedCost ?*NO_COST*) ; Unsetted acumulated cost
    					 (rules ?rules&~:(?rules isEmpty)) ; No beginning specification
    					 (childrenReady TRUE) ; Ready
    					 (cuttedSpecification FALSE)) ; but not cutted
	;(not (SimpleSpecification (OBJECT ?spec) ; And its not a loop-cut
    ;        				  (rulesArray $? $?loop $?loop&:(< 0 (length$ $?loop)))))
    (BridgeSpecificatorGraph (OBJECT ?graph&:(?graph containsNode ?spec)))
	=>
    (store "bad-graph" ?graph)
    (store "bad-spec" ?spec)
	(testFailed
        	"test-no-acumulated-cost"
			"A Specification has no acumulated cost"
			(?spec toString)))

(defrule test-no-specification-without-workingRule
        "It tests that there'is no specification without working rule."
    (or
        (runtime-tests)
        (and (result-tests) (process-complete (successfully TRUE))))
    (SimpleSpecification (workingRule nil) (rules ?rules&~:(?rules isEmpty)) (OBJECT ?spec))
    =>
    (testFailed
            "test-no-specification-without-workingRule"
    		"This specification has no working rule:"
    		(?spec toString)))

(defrule test-acumulated-cost-always-increases
    "Test that acumulated cost always increases"
    (runtime-tests) ;; @tofix Include result-tests as well but Jess throws an exception.
    (SimpleSpecification (OBJECT ?spec) (acumulatedCost ?ac1&~?*NO_COST*))
    (SimpleSpecification (OBJECT ?other) (acumulatedCost ?ac2&:(>= ?ac1 ?ac2)&~?*NO_COST*))
    (BridgeSpecificatorGraph  ;; They are parent and child.
            (allNodes ?nodes&:(?nodes contains ?spec))
            (OBJECT ?graph&:((?graph getChildren ?spec) contains ?other)))
	(not (SimpleSpecification ;; No other shorter path.
        	(acumulatedCost ?ac3&:(< ?ac3 ?ac2)&:(<> ?ac3 ?*NO_COST*))
			(OBJECT ?alt
    			&:(?nodes contains ?alt)
				&:((?graph getChildren ?alt) contains ?other))))
	=>
    (store "bad-graph" ?graph)
    (store "bad-spec" ?spec)
	(testFailed
        	"test-acumulated-cost-always-increases"
			"Acumulated cost didn't increase"
			(str-cat "From: " (?spec toString) "\nTo: " (?other toString))))

(defrule test-appropriate-accumulated-cost
    "Test the exact value of the accumulated costs. This only works if repeated actions are also included."
    (or
        (runtime-tests)
        (and 
            (result-tests)
            (process-complete (successfully TRUE))))
    ;; This test only works if repeated actions are also included. It must to be: COST_ALL_OCCURRENCES_COUNT = true
    (test (eq "true" ?*COST_ALL_OCCURRENCES_COUNT*))
	(SimpleSpecification (OBJECT ?spec) (actionsCount ?actions) (rulesArray $?rules)
        (acumulatedCost ?ac&:(<> ?ac ?*NO_COST*)&:(<> ?ac 
        (+ (* ?*INITIAL_COST* ?actions) (closedRulesHeuristic ?spec $?rules)))))
    (SimpleSpecification (OBJECT ?parent))
    (BridgeSpecificatorGraph (allNodes ?nodes&:(?nodes contains ?parent)&:(?nodes contains ?spec))
        (OBJECT ?graph&:((?graph getChildren ?parent) contains ?spec)))
    ; Avoid parent of merged nodes. They have a strage accumulated cost.
    (not
    	(SimpleSpecification (merged TRUE) (OBJECT ?merged&:(?nodes contains ?merged)&:((?graph getChildren ?spec) contains ?merged))))
    =>
    (testFailed "test-appropriate-accumulated-cost" "Accumulated cost is not as expected"
            (str-cat "Specification: " (?spec toString) "Expected accumulated cost = " 
                (+ (* ?*INITIAL_COST* ?actions) (closedRulesHeuristic ?spec $?rules)) "-------- Parent:" ?parent)))

(defrule test-no-heritage
        "It tests that there are no Graphs out of any heritage line."
    (process-complete)
    (result-tests)
	(BridgeSpecificatorGraph (OBJECT ?graph))
    (BridgeSpecificatorGraph (OBJECT ~?graph)) ; It's not a single graph Specification.
	(not (heritage $? ?graph $?))
	=>
	(testFailed 
        		"test-no-heritage"
				"A Graph isn't related to any other"
				?graph))

(defrule test-no-heritage-first-antecesor
        "It tests that all Graphs must have the same first antecesor."
    (or (runtime-tests) (and (result-tests) (process-complete (successfully TRUE))))
    (heritage ?antecesor $?)
	(heritage ?other&~?antecesor $?)
	=>
	(testFailed
        		"test-no-heritage-first-antecesor"
				"Two heritages found with diferent antecesor"
				(str-cat (?antecesor toString) (?other toString))))

(defrule test-no-rule-without-specification
        "It tests that every rule is within an specification"
    ;(not (adding-specification))
    (process-complete) ; @todo I'd rather the test being run during the process and not at the end.
    (result-tests)
    (DefaultRule (OBJECT ?rule))
    (not (SimpleSpecification (rulesArray $? ?rule $?)))
	=>
    (testFailed
            "test-no-rule-without-specification"
    		"A rule is out of any specification" (?rule toString)))

(defrule test-all-rules-within-sets
        "It tests that every rule of a Specification is within a Set"
    (runtime-tests)
    (SimpleSpecification (rules ?rules&~:(?rules isEmpty)&~:(instanceof ?rules java.util.Set))
                		 (OBJECT ?spec))
    =>
    (testFailed
            "test-all-rules-within-sets"
    		"Some specification rules are not within a Set."
    		(str-cat (?spec toString) " - " ((?rules getClass) getName))))
        
(defrule test-well-estimated-heuristic
        "It tests wether a new graph has a good initial estimation or not"
	(declare (salience -20))
    (runtime-tests)
    (SimpleSpecification (merged TRUE) (OBJECT ?merged) (acumulatedCost ?ac&~?*NO_COST*) (heuristic ?h))
    (BridgeSpecificatorGraph (OBJECT ?graph&:(?graph containsNode ?merged)) (start ?root))
	(SimpleSpecification (OBJECT ?root) (heuristic ?rh))
    (test
        (<> (+ ?ac ?h) ?rh))
    =>
    (log "debug" "A merged graph has an unaccurate heuristic. Estimation: " ?rh " Real: " (+ ?ac ?h)))    

(defrule test-passed
    "All test have been passed"
    (declare (salience -1100))
    (result-tests)
	(process-complete (successfully TRUE))
	=>
	(printout t " -- All Tests passed --" crlf))

;; E) Results ==============================================

(defrule specification-is-solution
    "Asserts a solution Specification"
    (declare (salience -15)) ; In order to heuristic and cost to be evaluated
    (JPowerBehaviorNode (OBJECT ?aEnd) (nodeType "EXIT"))
    (JPowerBehaviorNode (OBJECT ?otherEnd&~?aEnd) (nodeType "EXIT"))
    (actions-required (actions $?minimalActions))
	(SimpleSpecification
        (leftPath $? ?aEnd) (rightPath $? ?otherEnd)
		(actions $?actions&:(or
                (and
                    (neq "true" ?*ADAPT_EVERY_ACTION*)
                    (subsetp $?minimalActions $?actions)
                    ; This only works if actions and minimalActions are sets...
                    ; ...otherwise it should be (subsetp $?actions $?minimalActions).
                    (= (length$ $?actions) (length$ $?minimalActions)))
                (and
                    (eq "true" ?*ADAPT_EVERY_ACTION*)
                    (= (length$ $?actions) ?*ACTIONS_TO_ADAPT*))))
    	(OBJECT ?specification)
    	(solution FALSE))
    (not (ignore-solution (specification ?specification)))
    (BridgeSpecificatorGraph (OBJECT ?graph&:(?graph containsNode ?specification)))
	=>
    (?specification setSolution TRUE)
	(assert (solution (specification ?specification) (specificationGraph ?graph)))
	(store "solution" ?specification)
    (store "closest-specification" ?specification)
    (store "closest-specification-graph" ?graph)
    (log "debug" " ---------- Partial Solution Found (" (++ ?*SOLUTION_COUNTER*) "/" ?*GRAPH_COUNTER* ") ----------" crlf 
                " >Costs and heuristics may not be definitive< " crlf
    			?specification)
    (printout t " ---------- Partial Solution Found (" (++ ?*SOLUTION_COUNTER*) "/" ?*GRAPH_COUNTER* ") ---------- " crlf 
                " >Costs and heuristics may not be definitive< " crlf
    			(?specification toString) crlf))

;; @todo: I guess this rule can be removed.
(defrule specification-is-closest-solution
    	"When process is uncomplete all solutions found are displayed as best solutions"
    (declare (salience -1109))
	(process-complete)
	(not
    	(and
    		(solution (specification ?specification))
			(SimpleSpecification (acumulatedCost ?ac&:(<> ?ac ?*NO_COST*)) (OBJECT ?specification))))
	?f <- (solution (specification ?specification) (best-solution FALSE))
	=>
    (modify ?f (best-solution TRUE))
	(?specification setBestSolution TRUE))

(defrule specification-is-best-solution
    "It picks up the best solution among all of them"
    (declare (salience -1110))
	(process-complete)
	?f <- (solution (specification ?specification) (specificationGraph ?graph))
	(SimpleSpecification (OBJECT ?specification) (heuristic ?h) (acumulatedCost ?c&~?*NO_COST*) (rules ?rules))
    (not 
         (or 
             (and
                    (solution (specification ?other&~?specification))
		            (SimpleSpecification
		                (OBJECT ?other)
		        		(heuristic ?h2)
						(acumulatedCost ?c2
                            &~-1 ; Why I can't put '?*NO_COST*' here!?
                			&:(< (+ ?h2 ?c2) (+ ?h ?c)))))
         	 (and
                    (solution (specification ?other) (best-solution TRUE))
                	(SimpleSpecification (OBJECT ?other) (rules ?rules)))))
	=>
    (modify ?f (best-solution TRUE))
    (?specification setBestSolution TRUE)
	(store "solution" ?specification)
	(store "closest-specification" ?specification)
    (store "closest-specification-graph" ?graph)
	(printout t " ** Best solution found **" crlf))

;; F) Ignore current solutions ================================

(defrule ignore-current-solutions
    "Ignores and retract current solutions"
    (declare (salience 100)) ; Before solution calculations
    (ignore-current-solutions)
    ?s <- (solution (specification ?specification) (best-solution TRUE))
    =>
    (?specification setSolution FALSE)
    (?specification setBestSolution FALSE)
    (assert (ignore-solution (specification ?specification)))
    (retract ?s))

(defrule retract-process-complete
    "Retracts the process-complete fact"
    (declare (salience 100))
    (ignore-current-solutions)
    ?f <- (process-complete)
    =>
    (retract ?f))

(defrule retract-ignore-current-solutions
    "Retracts ignore-current-solutions because it has been already processed."
    (declare (salience 90))
    ?f <- (ignore-current-solutions)
    =>
    (retract ?f))

;; Other rules ==============================================

(defrule calculate-graph-count
        "It updates the graph counter."
    (declare (salience 900))
    (BridgeSpecificatorGraph)
	=>
	(++ ?*GRAPH_COUNTER*))

(defrule calculate-adaptative-actions
        "It calculates how many actions are needed to adapt."
    (declare (salience 100))
    (JPowerBehaviorNode (nodeType "SEND"|"RECEIVE"))
    =>
    (++ ?*ACTIONS_TO_ADAPT*))

(defrule calculate-expanded-nodes
    "It updates expanded nodes counter"
	(SimpleSpecification (childrenReady TRUE))
	=>
	(++ ?*EXPANDED_NODES*))

(defrule calculate-specifications
    "It calculates how many specifications exist so far."
    (SimpleSpecification)
    =>
    (++ ?*SPECIFICATION_COUNTER*))

(defrule calculate-rules
        "It counts the number of generated rules"
    (DefaultRule)
    =>
    (++ ?*RULES_COUNTER*))    
    
(defrule cancel-process
        "It finishes the process in a consistent state."
    (declare (salience -1000))
	(cancel-process)
	=>
	(assert (process-complete (successfully FALSE))))

(defrule no-activations
        "Fired when there're no other activations."
    (declare (salience -1000))
    =>
    (printout t "---- No pending rules yet ----" crlf))

(defrule the-end
        "Everything done and fisnished."
    (declare (salience -2000))
	(process-complete)
	=>
	(printout t "==== Bye bye ====" crlf)
	(halt))

;; Temporal rules ============================================
