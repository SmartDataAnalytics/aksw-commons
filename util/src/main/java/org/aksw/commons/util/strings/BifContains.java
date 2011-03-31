package org.aksw.commons.util.strings;

/**
 * takes a user input search string and makes a string that can be piped into
 * Filter (bif:contains(?s,'HERE') )
 * and produce valid output.
 * currently not so much is implemented, because I needed only the simplest case
 * and
 *
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class BifContains {
    private final String search;

    public BifContains(String search) {
        String tmp = search;
        while(tmp.contains("  ")){
            tmp = tmp.replaceAll("  ", " ");
        }

        this.search = tmp;

    }

    /**
     *
     * @return true if no white space
     */

    public boolean isSingle() {
        return !search.contains(" ");
    }

    public String makeWithAnd(){
        if(isSingle()){
            return makeTerm(search);
        }
        StringBuffer sb = new StringBuffer();
        String[] s = search.split(" ");
        for(int i = 0; i<s.length;i++){
            String current = s[i];
            boolean last = (i==s.length-1);
            sb.append(makeTerm(current));
            if(!last){
                sb.append("and");
            }
            

        }
        return sb.toString().trim();
    }


    /**
     * all the options can go here,
     * e.g. with a *
     * @param s
     * @return
     */
    private String makeTerm(String s){
        return " \""+s+"\" ";
    }



}
