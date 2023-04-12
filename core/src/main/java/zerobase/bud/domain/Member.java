package zerobase.bud.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import zerobase.bud.type.MemberStatus;

@Entity(name = "MEMBER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String userId;
    private String email;

    @OneToOne
    private Level level;

    private String nickname;

    private String profileImg;

    private String job;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;
    private String introduceMessage;

    private String oAuthAccessToken;
    private boolean addInfoYn;

    public Member update(String oAuthAccessToken) {
        this.oAuthAccessToken = oAuthAccessToken;

        return this;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> roles = new ArrayList<>();
        roles.add(this.getStatus().getKey());
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
