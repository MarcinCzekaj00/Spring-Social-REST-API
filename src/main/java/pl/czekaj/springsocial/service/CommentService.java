package pl.czekaj.springsocial.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.czekaj.springsocial.dto.CommentDto;
import pl.czekaj.springsocial.dto.mapper.CommentDtoMapper;
import pl.czekaj.springsocial.exception.commentException.AddCommentException;
import pl.czekaj.springsocial.exception.commentException.CommentNotFoundException;
import pl.czekaj.springsocial.exception.postException.PostNotFoundException;
import pl.czekaj.springsocial.model.Comment;
import pl.czekaj.springsocial.repository.CommentRepository;
import pl.czekaj.springsocial.repository.PostRepository;

import javax.transaction.Transactional;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {

    private static final int PAGE_SIZE = 50;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public List<CommentDto> getComments(int page, Sort.Direction sort) {
        List<Comment> comments = commentRepository.findAllComments(PageRequest.of(page, PAGE_SIZE, Sort.by(sort, "timeCreated")));
        if(comments.isEmpty()) throw new CommentNotFoundException();
        return CommentDtoMapper.mapToCommentDtos(comments);
    }

    public List<CommentDto> getCommentsFromPost(Long postId, int page, Sort.Direction sort) {
        List<Comment> comments = commentRepository.findAllByPostId(postId,PageRequest.of(page, PAGE_SIZE, Sort.by(sort, "timeCreated")));
        if(comments.isEmpty()) throw new CommentNotFoundException();
        return CommentDtoMapper.mapToCommentDtos(comments);
    }

    public CommentDto getSingleComment(Long postId,Long commentId){
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        Comment comment = commentRepository.findByPostIdAndCommentId(postId,commentId);
        return CommentDtoMapper.mapToCommentDtos(comment);
    }

    @Transactional
    public CommentDto addComment(Comment comment,Long postId){
        try{
            comment.setPostId(postId);
            commentRepository.save(comment);
        } catch(Throwable e){
            throw new AddCommentException();
        }
        return CommentDtoMapper.mapToCommentDtos(comment);
    }

    @Transactional
    public CommentDto editComment(Comment comment,Long postId){
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        commentRepository.findById(comment.getCommentId()).orElseThrow(() -> new CommentNotFoundException(comment.getCommentId()));
        Comment editedComment = commentRepository.findByPostIdAndCommentId(postId, comment.getCommentId());
        editedComment.setContent(comment.getContent());
        commentRepository.save(comment);
        return CommentDtoMapper.mapToCommentDtos(comment);
    }

    @Transactional
    public CommentDto editSingleComment(Comment comment,Long postId,Long commentId){
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(comment.getCommentId()));
        comment.setContent(comment.getContent());
        comment.setPostId(postId);
        comment.setCommentId(commentId);
        commentRepository.save(comment);
        return CommentDtoMapper.mapToCommentDtos(comment);
    }

    @Transactional
    public void deleteComment(Long postId,Long commentId){
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        commentRepository.deleteByCommentIdAndPostId(commentId,postId);
    }

}
