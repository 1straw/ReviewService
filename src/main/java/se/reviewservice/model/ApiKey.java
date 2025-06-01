package se.reviewservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "api_keys")
public class ApiKey {
    @Id
    private String key;
    private String companyId;
    private List<String> roles;
    private int usageCount;

}
