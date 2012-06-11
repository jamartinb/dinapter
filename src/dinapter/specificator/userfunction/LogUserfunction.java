/*
 * This file is part of Dinapter.
 *
 *  Dinapter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Dinapter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  (C) Copyright 2007 José Antonio Martín Baena
 *  
 *  José Antonio Martín Baena <jose.antonio.martin.baena@gmail.com>
 *  Ernesto Pimentel Sánchez <ernesto@lcc.uma.es>
 */
/*
 * LogUserfunction.java
 *
 * Created on 29 de mayo de 2007, 15:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.specificator.userfunction;

import jess.Context;
import jess.JessException;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

import org.apache.log4j.Logger;

/**
 *
 * @author arkangel
 */
public class LogUserfunction implements Userfunction{
    private final Logger log;
    
    /** Creates a new instance of LogUserfunction */
    public LogUserfunction(String logger) {
        log = Logger.getLogger(logger);
    }
    
    public String getName() {
        return "log";
    }
    
    public Value call(ValueVector vv, Context context) throws JessException {
        String type = vv.get(1).symbolValue(context);
        String message = getMessage(vv, context);
        if (type.equals("info"))
            log.info(message);
        else if (type.equals("debug"))
            log.debug(message);
        else if (type.equals("error"))
            log.error(message);
        else if (type.equals("warn"))
            log.warn(message);
        else if (type.equals("trace"))
            log.trace(message);
        else if (type.equals("fatal"))
            log.fatal(message);
        else
            throw new JessException("log","Log type unknown:",type);
        return null;
    }

	private String getMessage(ValueVector vv, Context context)
			throws JessException {
		String message = "";
        for (int i = 2; i < vv.size();i++) {
            if (vv.get(i).isNumeric(context))
                message += vv.get(i).floatValue(context);
            else if (vv.get(i).isLexeme(context)) {
                String value = vv.get(i).stringValue(context);
                message += "crlf".equals(value)?"\n":value;
            /*
            }  else if (vv.get(i).type() == RU.MULTIVARIABLE) {
            	message += getMessage(vv.get(i).listValue(context),context);
             */
            } else  { // I suppose it's an object.
            	message += vv.get(i).javaObjectValue(context);
            }
        }
		return message;
	}
}
