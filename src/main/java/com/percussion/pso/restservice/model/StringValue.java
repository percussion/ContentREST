/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;


/**
 */
@XmlRootElement(name="Value")
public class StringValue implements Value {

	/**
	 * Field stringValue.
	 */
	private String stringValue;
	
	
	/**
	 * Field type.
	 */
	public static final int TYPE=0;  
	
	/**
	 * Method getStringValue.
	 * @return String
	 * @see com.percussion.pso.restservice.model.Value#getStringValue()
	 */
	@XmlValue
	public String getStringValue() {
		return stringValue;
	}
	/**
	 * Method setStringValue.
	 * @param stringValue String
	 * @see com.percussion.pso.restservice.model.Value#setStringValue(String)
	 */
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

}
