/**
* Copyright (c) 2009, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package clear.pos;


/**
 * English part-of-speech tag library.
 * @author Jinho D. Choi
 * <b>Last update:</b> 4/14/2010
 */
public class PosEnLib
{
	/** Pos-tag of nouns, singular or mass */
	static public String NN   = "NN";
	/** Pos-tag of verbs, base form */
	static public String VB   = "VB";
	/** Pos-tag of adjectives */
	static public String JJ   = "JJ";
	/** Pos-tag of adverbs */
	static public String RB   = "RB";
	/** Pos-tag of modals */
	static public String MD   = "MD";
	/** Pos-tag of wh-determiners */
	static public String WDT  = "WDT";
	/** Pos-tag of wh-pronouns */
	static public String WP   = "WP";
	/** Pos-tag of wh-adverbs */
	static public String WRB  = "WRB";
	/** Pos-tag of preposition */
	static public String IN   = "IN";
	
	/** @return true if <code>pos</code> is a noun. */
	static public boolean isNoun(String pos)
	{
		return pos.startsWith(NN);
	}
	
	/** @return true if <code>pos</code> is a verb. */
	static public boolean isVerb(String pos)
	{
		return pos.startsWith(VB);
	}
	
	/** @return true if <code>pos</code> is an adjective. */
	static public boolean isAdjective(String pos)
	{
		return pos.startsWith(JJ);
	}
	
	/** @return true if <code>pos</code> is an adverb. */
	static public boolean isAdverb(String pos)
	{
		return pos.startsWith(RB);
	}
	
	/** @return true if <code>pos</code> is a modal. */
	static public boolean isModal(String pos)
	{
		return pos.equals(MD);
	}
	
	/** @return coarse-grained pos of <code>pos</code>. */
	static public String cpos(String pos)
	{
		return (pos.length() < 2) ? pos : pos.substring(0, 2);
	}
}