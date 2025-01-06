package projetvue.springboot_backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Document(collection = "poll")
public class Poll {
    @Id
    private String id;

    private String title;

    private List<String> options;

    private Map<String, Integer> votes;

    private Map<String, String> voters; // Maps userId to their chosen option

    public Poll() {
        this.votes = new HashMap<>();
        this.voters = new HashMap<>();
    }

}
