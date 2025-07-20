package fsa.project.online_shop.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VnPayResponse {
    private String status;
    private String message;
    private String url;
}
