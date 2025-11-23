package com.plakhotnikov.cloudkeeper.validation;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
// TODO: how to make it better? Need to be easy-accessible and does not violate encapsulation
public class ValidationProperties {

    public Patterns patterns;
    public Messages messages;

    @Setter
    public static class Patterns {
        public String username;
        public String password;
        public String objname;
        public String search;
    }

    @Setter
    public static class Messages {
        public String username;
        public String password;
        public String objname;
        public String search;
    }

}
