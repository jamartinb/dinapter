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
(require rules/rules)

(deftemplate step
    (declare (ordered TRUE)))

(defrule wee-3
    (disabled)
    (JPowerBehaviorNode (description "login") (OBJECT ?login))
	(JPowerBehaviorNode (description "user") (OBJECT ?user))
	(JPowerBehaviorNode (description "download") (OBJECT ?download))
	(JPowerBehaviorNode (description "data") (OBJECT ?data))
    (JPowerBehaviorNode (description "result") (OBJECT ?result))
    (JPowerBehaviorNode (description "connected") (OBJECT ?connected))
	(JPowerBehaviorNode (description "getFile") (OBJECT ?getFile))
	(JPowerBehaviorNode (description "noSuchFile") (OBJECT ?noSuchFile))
	(JPowerBehaviorNode (description "quit") (OBJECT ?quit))
	;(JPowerBehaviorNode (description "rejected") (OBJECT ?rejected))
	(DefaultRule (OBJECT ?a) (leftSideArray ?login) (rightSideArray ?user))
    (or
		(SimpleSpecification (OBJECT ?specification) (rulesArray ?a))
		;(and
		;	(DefaultRule (OBJECT ?b) (leftSideArray ?download) (rightSideArray ?rejected))
		;	(SimpleSpecification (OBJECT ?specification) (rulesArray ?a ?b)))
		;(and
		;	(DefaultRule (OBJECT ?b) (leftSideArray ?download ?data) (rightSideArray ?rejected))
        ;	(SimpleSpecification (OBJECT ?specification) (rulesArray ?a ?b)))
		(and
			(DefaultRule (OBJECT ?b) (leftSideArray) (rightSideArray ?connected))
			(or
				(SimpleSpecification (OBJECT ?specification) (rulesArray ?a ?b))
                (SimpleSpecification (OBJECT ?specification) (rulesArray ?b ?a))
            	(and
					(DefaultRule (OBJECT ?c) (leftSideArray ?download) (rightSideArray))
					(SimpleSpecification (OBJECT ?specification) (rules ?rules&:(= 3 (?rules size))&:(?rules contains ?a)&:(?rules contains ?b)&:(?rules contains ?c))))
				(and
					(DefaultRule (OBJECT ?c) (leftSideArray ?download) (rightSideArray ?getFile))
                	(or
						(SimpleSpecification (OBJECT ?specification) (rules ?rules&:(= 3 (?rules size))&:(?rules contains ?a)&:(?rules contains ?b)&:(?rules contains ?c)))
						(and
                            (or
								(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray))
								(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?result))
								(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?noSuchFile)))
							(SimpleSpecification (OBJECT ?specification) (rules ?rules&:(?rules equals (new java.util.HashSet (call java.util.Arrays asList (create$ ?a ?b ?c ?d)))))))
						(and
    						(or
    							(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?result))
								(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?noSuchFile)))
                        	(DefaultRule (OBJECT ?e) (leftSideArray) (rightSideArray ?quit))
                        	(SimpleSpecification (OBJECT ?specification) (rules ?rules&:(?rules equals (new java.util.HashSet (call java.util.Arrays asList (create$ ?a ?b ?c ?d ?e))))))
                            ))))))
    (SimpleSpecification (OBJECT ?specification) 
        (childrenReady TRUE)
        (copiedSpecification FALSE)
        (cuttedSpecification ?))
	(BridgeSpecificatorGraph (OBJECT ?graph&:(?graph containsNode ?specification)))
	=>
    (assert (step ?specification))
	(log "info" "WEE- " (?specification toString)))

(defrule wee-FULL
    (declare (salience -99))
    (JPowerBehaviorNode (description "login") (OBJECT ?login))
	(JPowerBehaviorNode (description "user") (OBJECT ?user))
	(JPowerBehaviorNode (description "download") (OBJECT ?download))
	(JPowerBehaviorNode (description "data") (OBJECT ?data))
    (JPowerBehaviorNode (description "result") (OBJECT ?result))
    (JPowerBehaviorNode (description "connected") (OBJECT ?connected))
	(JPowerBehaviorNode (description "getFile") (OBJECT ?getFile))
	(JPowerBehaviorNode (description "noSuchFile") (OBJECT ?noSuchFile))
	(JPowerBehaviorNode (description "quit") (OBJECT ?quit))
	(JPowerBehaviorNode (description "rejected") (OBJECT ?rejected))
	(DefaultRule (OBJECT ?a) (leftSideArray ?login) (rightSideArray ?user))
    (or
		(SimpleSpecification (OBJECT ?specification) (rulesArray ?a))
		(and
			(DefaultRule (OBJECT ?b) (leftSideArray ?download) (rightSideArray ?rejected))
			(SimpleSpecification (OBJECT ?specification) (rules ?rules&:(= 2 (?rules size))&:(?rules contains ?a)&:(?rules contains ?b))))
		(and
            (DefaultRule (OBJECT ?b) (leftSideArray ?download ?data) (rightSideArray ?rejected))
	   		(SimpleSpecification (OBJECT ?specification) (rules ?rules&:(= 2 (?rules size))&:(?rules contains ?a)&:(?rules contains ?b))))
		(and
			(DefaultRule (OBJECT ?b) (leftSideArray) (rightSideArray ?connected))
			(or
				(SimpleSpecification (OBJECT ?specification) (rulesArray ?a ?b))
                (SimpleSpecification (OBJECT ?specification) (rulesArray ?b ?a))
           		(and
				
					(DefaultRule (OBJECT ?c) (leftSideArray ?download) (rightSideArray ?getFile))
                	(or
						(SimpleSpecification (OBJECT ?specification) (rules ?rules&:(= 3 (?rules size))&:(?rules contains ?a)&:(?rules contains ?b)&:(?rules contains ?c)))
                    	(and
				           (or
								(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray))
								(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?result))
								(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?noSuchFile)))
								(SimpleSpecification (OBJECT ?specification) (rules ?rules&:(?rules equals (new java.util.HashSet (call java.util.Arrays asList (create$ ?a ?b ?c ?d)))))))
						(and
    						(or
    							(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?result))
								(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?noSuchFile)))
                        	(DefaultRule (OBJECT ?e) (leftSideArray) (rightSideArray ?quit))
                            (SimpleSpecification (OBJECT ?specification) (rules ?rules&:(?rules equals (new java.util.HashSet (call java.util.Arrays asList (create$ ?a ?b ?c ?d ?e)))))))
                        (and
                            (DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?result))
							(DefaultRule (OBJECT ?e) (leftSideArray ?data) (rightSideArray ?noSuchFile))
                            (DefaultRule (OBJECT ?f) (leftSideArray) (rightSideArray ?quit))
                        	(or
                                (SimpleSpecification (OBJECT ?specification) (rules ?rules&:(?rules equals (new java.util.HashSet (call java.util.Arrays asList (create$ ?a ?b ?c ?d ?e ?f))))))
                                (and
                                    (DefaultRule (OBJECT ?g) (leftSideArray ?download ?data) (rightSideArray ?rejected))
                            		(SimpleSpecification (OBJECT ?specification) (rules ?rules&:(?rules equals (new java.util.HashSet (call java.util.Arrays asList (create$ ?a ?b ?c ?d ?e ?f ?g))))))))))))))
    (SimpleSpecification (OBJECT ?specification) 
        ;(childrenReady TRUE) ; @todo Uncomment
        (copiedSpecification FALSE)
        ;(acumulatedCost ?ac&:(> ?ac 0))
        ;(merged TRUE) ; @todo Remove line
        (cuttedSpecification ?))
    (SimpleSpecification (OBJECT ?parent))
	(BridgeSpecificatorGraph (OBJECT ?graph&:(?graph containsNode ?specification)&:(?graph containsNode ?parent)&:((?graph getChildren ?parent) contains ?specification)))
	=>
    (assert (step ?specification))
	(log "info" "WEE-FULL Parent: " (?parent toString) "Children: " (?specification toString)))

(defrule wee-2
    (disabled)
    (JPowerBehaviorNode (description "login") (OBJECT ?login))
	(JPowerBehaviorNode (description "user") (OBJECT ?user))
	(JPowerBehaviorNode (description "download") (OBJECT ?download))
	(JPowerBehaviorNode (description "data") (OBJECT ?data))
    (JPowerBehaviorNode (description "result") (OBJECT ?result))
    (JPowerBehaviorNode (description "connected") (OBJECT ?connected))
	(JPowerBehaviorNode (description "getFile") (OBJECT ?getFile))
	(JPowerBehaviorNode (description "noSuchFile") (OBJECT ?noSuchFile))
	(JPowerBehaviorNode (description "quit") (OBJECT ?quit))
	(DefaultRule (OBJECT ?a) (leftSideArray ?data) (rightSideArray ?noSuchFile))
	(DefaultRule (OBJECT ?b) (leftSideArray ?login) (rightSideArray ?user))
    (DefaultRule (OBJECT ?c) (leftSideArray) (rightSideArray ?connected))
    (DefaultRule (OBJECT ?d) (leftSideArray ?download) (rightSideArray ?getFile))
    (DefaultRule (OBJECT ?e) (leftSideArray ?data) (rightSideArray ?result))
    (DefaultRule (OBJECT ?f) (leftSideArray) (rightSideArray ?quit))
    (SimpleSpecification (rulesArray $?rules
            &:(subsetp (create$ ?a ?b ?c ?d ?e ?f) $?rules)
        	&:(subsetp $?rules (create$ ?a ?b ?c ?d ?e ?f)))
		(OBJECT ?spec))
    (BridgeSpecificatorGraph (OBJECT ?graph&:(?graph containsNode ?spec)))
    =>
    (assert (step ?spec))
    (log "info" "2- " (?spec toString)))

(defrule wee-1
    (declare (salience 10))
    (disabled)
    (step ?step)
    (step ?other)
    (specifications-to-merge (from ?step) (to ?other))
    (SimpleSpecification (OBJECT ?step) (actions $?actions))
    (SimpleSpecification (OBJECT ?other) (actions $?others))
    (test (= ?*ACTIONS_TO_ADAPT* (length$ (union$ $?actions $?others))))
	=>
    ;(watch rules)
    (log "fatal" "1- " "*****************************" (?step toString) (?other toString)))

(defrule wee-4
    (disabled)
    (JPowerBehaviorNode (description "login") (OBJECT ?login))
	(JPowerBehaviorNode (description "user") (OBJECT ?user))
	(JPowerBehaviorNode (description "download") (OBJECT ?download))
	(JPowerBehaviorNode (description "data") (OBJECT ?data))
    (JPowerBehaviorNode (description "result") (OBJECT ?result))
    (JPowerBehaviorNode (description "connected") (OBJECT ?connected))
	(JPowerBehaviorNode (description "getFile") (OBJECT ?getFile))
	(JPowerBehaviorNode (description "noSuchFile") (OBJECT ?noSuchFile))
	(JPowerBehaviorNode (description "quit") (OBJECT ?quit))
	(JPowerBehaviorNode (description "rejected") (OBJECT ?rejected))
	(DefaultRule (OBJECT ?a) (leftSideArray ?login) (rightSideArray ?user))
	(DefaultRule (OBJECT ?b) (leftSideArray) (rightSideArray ?connected))
	(DefaultRule (OBJECT ?c) (leftSideArray ?download) (rightSideArray ?getFile))
	(DefaultRule (OBJECT ?d) (leftSideArray ?data) (rightSideArray ?result))
	(DefaultRule (OBJECT ?e) (leftSideArray ?data) (rightSideArray ?noSuchFile))
	(DefaultRule (OBJECT ?f) (leftSideArray ?download ?data) (rightSideArray ?rejected))
	(SimpleSpecification (rulesArray $?rules
        &:(subsetp $?rules (create$ ?a ?b ?c ?d ?e ?f))
		&:(subsetp (create$ ?a ?b ?c ?d ?e ?f) $?rules)) (costReady TRUE) (OBJECT ?spec))
	(BridgeSpecificatorGraph (OBJECT ?graph&:(?graph containsNode ?spec)))
	=>
    (log "fatal" "4- " (?spec toString))
    (assert (step ?spec)))

(printout t "Debug rules loaded." crlf)
