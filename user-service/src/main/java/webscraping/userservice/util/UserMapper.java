package webscraping.userservice.util;

import lombok.experimental.UtilityClass;
import webscraping.userservice.dto.UserDto;
import webscraping.userservice.model.User;

@UtilityClass
public class UserMapper {

    public static UserDto userToDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId().toString());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());

        return userDto;
    }
}
