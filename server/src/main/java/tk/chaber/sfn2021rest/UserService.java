package tk.chaber.sfn2021rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.chaber.sfn2021rest.persistence.entity.BoardRecord;
import tk.chaber.sfn2021rest.persistence.entity.User;
import tk.chaber.sfn2021rest.persistence.entity.VerificationToken;
import tk.chaber.sfn2021rest.persistence.repository.BoardRepo;
import tk.chaber.sfn2021rest.web.dto.BoardRecordDto;
import tk.chaber.sfn2021rest.web.dto.RegisterUserDto;
import tk.chaber.sfn2021rest.web.dto.UserDto;
import tk.chaber.sfn2021rest.web.error.*;
import tk.chaber.sfn2021rest.persistence.repository.UserRepo;
import tk.chaber.sfn2021rest.persistence.repository.VerificationTokenRepo;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepo userRepository;

    @Autowired
    private VerificationTokenRepo tokenRepository;

    @Autowired
    private BoardRepo boardRepository;

    @Autowired
    private PasswordEncoder passEncoder;

    public User loginUserAccount(UserDto userDto) throws
            UserDoesNotExistException,
            UserNotVerifiedException,
            AuthenticationFailedException {
        if(!userRepository.existsByUsername(userDto.getUsername())){
            throw new UserDoesNotExistException("There is no user with such a username: " + userDto.getUsername());
        }

        User user = userRepository.findByUsername(userDto.getUsername());

        if(!user.isEnabled()){
            throw new UserNotVerifiedException("User has not verified their email address.");
        }

        if(!passEncoder.matches(userDto.getPassword(), user.getPassword())){
            throw new AuthenticationFailedException("Authentication failed");
        }

        user.setSecret(UUID.randomUUID().toString());

        return userRepository.save(user);
    }

    public User registerNewUserAccount(RegisterUserDto userDto) throws UserAlreadyExistsException {
        System.out.println(userDto.getEmail());
        if(userRepository.existsByEmail(userDto.getEmail())){
            throw new EmailAlreadyExistsException("There is an account with such an email address: " + userDto.getEmail());
        }

        if(userRepository.existsByUsername(userDto.getUsername())){
            throw new UserAlreadyExistsException("There is a user with such a username: " +userDto.getUsername());
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(encodePass(userDto.getPassword()));
        user.setEmail(userDto.getEmail());

        return userRepository.save(user);
    }

    public void saveRegisteredUser(User user){
        userRepository.save(user);
    }

    public User getUser(String username){
        return userRepository.findByUsername(username);
    }

    public String encodePass(String password){
        return passEncoder.encode(password);
    }



    public void createVerificationToken(User user, String token){
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);
    }

    public VerificationToken getVerificationToken(String token){
        return tokenRepository.findByToken(token);
    }


    public void saveBoardRecord(BoardRecordDto recordDto) throws
            UserDoesNotExistException,
            UserNotVerifiedException,
            AuthenticationFailedException{
        if(!userRepository.existsByUsername(recordDto.getUsername())){
            throw new UserDoesNotExistException("There is no user with such a username: " + recordDto.getUsername());
        }

        User user = getUser(recordDto.getUsername());

        if(!user.isEnabled()){
            throw new UserNotVerifiedException("User has not verified their email address.");
        }

        if(!user.validSecret(recordDto.getSecret())){
            throw new AuthenticationFailedException("Authentication failed");
        }

        if(boardRepository.existsByUser(user)){
            BoardRecord oldRecord = boardRepository.findByUser(user);
            long oldTime = oldRecord.getTime();
            long oldScore = oldRecord.getScore();

            String[] times = recordDto.getTime().split(":");
            long minutes = Integer.parseInt(times[0]);
            long seconds = Integer.parseInt(times[1]);

            long newTime = (60 * minutes + seconds);

            if(recordDto.getScore() > oldScore || (recordDto.getScore() == oldScore && newTime < oldTime)){
                oldRecord.setTime(newTime);
                oldRecord.setScore(recordDto.getScore());

                boardRepository.save(oldRecord);
            }
        }else {
            BoardRecord record = new BoardRecord(user, recordDto.getScore(), recordDto.getTime());

            boardRepository.save(record);
        }
    }

    public void saveCreatedRecord(BoardRecord record){
        boardRepository.save(record);
    }

    public BoardRecord readBoardRecord(String username) throws
            UserDoesNotExistException,
            NoUserRecordException {
        if(!userRepository.existsByUsername(username)){
            throw new UserDoesNotExistException("There is no user with such a username: " + username);
        }

        User user = getUser(username);

        if(!boardRepository.existsByUser(user)){
            throw new NoUserRecordException("This user does not have a record saved.");
        }

        return boardRepository.findByUser(user);
    }

    public List<BoardRecord> readBoard() {
        return boardRepository.findAll();
    }

    public List<BoardRecord> readTopBoard() {
        return boardRepository.findAll(PageRequest.of(0, 5, Sort.by("score").descending().and(Sort.by("time").ascending())));
    }
}
