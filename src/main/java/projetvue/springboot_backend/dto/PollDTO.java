package projetvue.springboot_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PollDTO {
    private String title; // Title of the poll
    private List<String> options; // List of options for the poll
    private Map<String, Integer> votes; // Votes for each option (optional for submission)
    private Map<String, String> voters; // Voters and their choices (optional for submission)

}
