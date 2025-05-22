package se.reviewservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group5ProductResponse {
    @JsonProperty("Id")  // Mappar till "Id" med stor bokstav från deras API
    private String id;

    @JsonProperty("Name")  // Mappar till "Name" med stor bokstav från deras API
    private String name;
}