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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import zerobase.bud.type.MemberStatus;

@Entity(name = "MEMBER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Member extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String userId;

    @OneToOne
    private Level level;

    @Column(unique = true)
    private String userCode;

    @Column(unique = true)
    private String nickname;

    private String profileImg;

    private String job;

    private String oauthToken;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;
    private String introduceMessage;

    private boolean addInfoYn;

    public Member update(String userCode, String oauthToken) {
        this.userCode = userCode;
        this.oauthToken = oauthToken;

        return this;
    }

    public static Member register(String userId, String userCode, String nickname, String token, String profileImg, Level level) {
        return Member.builder()
                .userId(userId)
                .userCode(userCode)
                .nickname(nickname)
                .oauthToken(token)
                .profileImg(profileImg)
                .level(level)
                .addInfoYn(false)
                .status(MemberStatus.VERIFIED)
                .build();
    }

    public void updateLevel(Level level) {
        this.level = level;
    }

    public void updateProfileImage(String imagePath) {
        System.out.println(imagePath);
        System.out.println(this.profileImg);
        this.profileImg = imagePath;
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
