package littleware.base;

import java.util.Map;
import java.util.HashMap;


/**
 * Enumeration for states of the USA
 */
public enum UsaState {
	AL {
         public String getFullName () {
             return "Alabama";
         }
    },
    AK {
         public String getFullName () {
             return "Alaska";
         }
    },
    AS {
         public String getFullName () {
             return "American Samoa";
         }
    },
    AZ {
         public String getFullName () {
             return "Arizona";
         }
    },
    AR {
         public String getFullName () {
             return "Arkansas";
         }
    },
    CA {
         public String getFullName () {
             return "California";
         }
    },
    CO {
         public String getFullName () {
             return "Colorado";
         }
    },
    CT {
         public String getFullName () {
             return "Connecticut";
         }
    },
    DE {
         public String getFullName () {
             return "Delaware";
         }
    },
    DC {
         public String getFullName () {
             return "District Of Columbia";
         }
    },
    FM {
         public String getFullName () {
             return "Federated States Of Micronesia";
         }
    },
    FL {
         public String getFullName () {
             return "Florida";
         }
    },
    GA {
         public String getFullName () {
             return "Georgia";
         }
    },
    GU {
         public String getFullName () {
             return "Guam";
         }
    },
    HI {
         public String getFullName () {
             return "Hawaii";
         }
    },
    ID {
         public String getFullName () {
             return "Idaho";
         }
    },
    IL {
         public String getFullName () {
             return "Illinois";
         }
    },
    IN {
         public String getFullName () {
             return "Indiana";
         }
    },
    IA {
         public String getFullName () {
             return "Iowa";
         }
    },
    KS {
         public String getFullName () {
             return "Kansas";
         }
    },
    KY {
         public String getFullName () {
             return "Kentucky";
         }
    },
    LA {
         public String getFullName () {
             return "Louisiana";
         }
    },
    ME {
         public String getFullName () {
             return "Maine";
         }
    },
    MH {
         public String getFullName () {
             return "Marshall Islands";
         }
    },
    MD {
         public String getFullName () {
             return "Maryland";
         }
    },
    MA {
         public String getFullName () {
             return "Massachusetts";
         }
    },
    MI {
         public String getFullName () {
             return "Michigan";
         }
    },
    MN {
         public String getFullName () {
             return "Minnesota";
         }
    },
    MS {
         public String getFullName () {
             return "Mississippi";
         }
    },
    MO {
         public String getFullName () {
             return "Missouri";
         }
    },
    MT {
         public String getFullName () {
             return "Montana";
         }
    },
    NE {
         public String getFullName () {
             return "Nebraska";
         }
    },
    NV {
         public String getFullName () {
             return "Nevada";
         }
    },
    NH {
         public String getFullName () {
             return "New Hampshire";
         }
    },
    NJ {
         public String getFullName () {
             return "New Jersey";
         }
    },
    NM {
         public String getFullName () {
             return "New Mexico";
         }
    },
    NY {
         public String getFullName () {
             return "New York";
         }
    },
    NC {
         public String getFullName () {
             return "North Carolina";
         }
    },
    ND {
         public String getFullName () {
             return "North Dakota";
         }
    },
    MP {
         public String getFullName () {
             return "Northern Mariana Islands";
         }
    },
    OH {
         public String getFullName () {
             return "Ohio";
         }
    },
    OK {
         public String getFullName () {
             return "Oklahoma";
         }
    },
    OR {
         public String getFullName () {
             return "Oregon";
         }
    },
    PW {
         public String getFullName () {
             return "Palau";
         }
    },
    PA {
         public String getFullName () {
             return "Pennsylvania";
         }
    },
    PR {
         public String getFullName () {
             return "Puerto Rico";
         }
    },
    RI {
         public String getFullName () {
             return "Rhode Island";
         }
    },
    SC {
         public String getFullName () {
             return "South Carolina";
         }
    },
    SD {
         public String getFullName () {
             return "South Dakota";
         }
    },
    TN {
         public String getFullName () {
             return "Tennessee";
         }
    },
    TX {
         public String getFullName () {
             return "Texas";
         }
    },
    UT {
         public String getFullName () {
             return "Utah";
         }
    },
    VT {
         public String getFullName () {
             return "Vermont";
         }
    },
    VI {
         public String getFullName () {
             return "Virgin Islands";
         }
    },
    VA {
         public String getFullName () {
             return "Virginia";
         }
    },
    WA {
         public String getFullName () {
             return "Washington";
         }
    },
    WV {
         public String getFullName () {
             return "West Virginia";
         }
    },
    WI {
         public String getFullName () {
             return "Wisconsin";
         }
    },
    WY {
         public String getFullName () {
             return "Wyoming";
         }
    },
	OUT_OF_USA {
		public String getFullName () {
			return "not USA";
		}
	};
	
	/** Return the full state name: WY = Wyoming, ... */
	public abstract String getFullName ();
	
	private static Map<String, UsaState>  ov_dictionary = new HashMap<String, UsaState> ();
	
	static {
		for ( UsaState n_state : UsaState.values () ) {
			ov_dictionary.put ( n_state.getFullName (), n_state );
			ov_dictionary.put ( n_state.toString (), n_state );
		}
	}
	
	/**
	 * Utility class - return the UsaState that
	 * matches the given state abbreviation or full name
	 *
	 * @return matching UsaState or null if no match
	 */
	public static UsaState parse ( String s_name ) {
		return ov_dictionary.get ( s_name );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

