package run.halo.app.service.impl;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.TagWithPostCountDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostTag;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.projection.TagPostPostCountProjection;
import run.halo.app.repository.PostRepository;
import run.halo.app.repository.PostTagRepository;
import run.halo.app.repository.TagRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostTagService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

/**
 * Post tag service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-19
 */
@Service
public class PostTagServiceImpl extends AbstractCrudService<PostTag, Integer>
    implements PostTagService {

    private final PostTagRepository postTagRepository;

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    private final OptionService optionService;

    public PostTagServiceImpl(PostTagRepository postTagRepository,
        PostRepository postRepository,
        TagRepository tagRepository,
        OptionService optionService) {
        super(postTagRepository);
        this.postTagRepository = postTagRepository;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.optionService = optionService;
    }

    @Override
    public List<Tag> listTagsBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        // Find all tag ids
        Set<Integer> tagIds = postTagRepository.findAllTagIdsByPostId(postId);

        return tagRepository.findAllById(tagIds);
    }

    @Override
    public List<TagWithPostCountDTO> listTagWithCountDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // Find all tags
        List<Tag> tags = tagRepository.findAll(sort);

        // Find all post count
        Map<Integer, Long> tagPostCountMap = ServiceUtils
            .convertToMap(postTagRepository.findPostCount(), TagPostPostCountProjection::getTagId,
                TagPostPostCountProjection::getPostCount);

        // Find post count
        return tags.stream().map(
            tag -> {
                TagWithPostCountDTO tagWithCountOutputDTO =
                    new TagWithPostCountDTO().convertFrom(tag);
                tagWithCountOutputDTO.setPostCount(tagPostCountMap.getOrDefault(tag.getId(), 0L));

                StringBuilder fullPath = new StringBuilder();

                if (optionService.isEnabledAbsolutePath()) {
                    fullPath.append(optionService.getBlogBaseUrl());
                }

                fullPath.append(URL_SEPARATOR)
                    .append(optionService.getTagsPrefix())
                    .append(URL_SEPARATOR)
                    .append(tag.getSlug())
                    .append(optionService.getPathSuffix());

                tagWithCountOutputDTO.setFullPath(fullPath.toString());

                return tagWithCountOutputDTO;
            }
        ).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, List<Tag>> listTagListMapBy(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        /*
        * 找到所有PostTag 就是post和tag关联表的内容
        * 然后获取其中的tagId
        * 再根据Id去获取所有tags
        * */

        // Find all post tags
        List<PostTag> postTags = postTagRepository.findAllByPostIdIn(postIds);

        // Fetch tag ids
        Set<Integer> tagIds = ServiceUtils.fetchProperty(postTags, PostTag::getTagId);

        // Find all tags
        List<Tag> tags = tagRepository.findAllById(tagIds);

        /*
         * 这里执行了传进来的方法Tag::getId作为他的key
         * 遍历list的每一个元素 执行getId作为key 然后该元素（data）作为value
         * 返回得到一个map
         * */
        // Convert to tag map
        Map<Integer, Tag> tagMap = ServiceUtils.convertToMap(tags, Tag::getId);

        // Create tag list map
        Map<Integer, List<Tag>> tagListMap = new HashMap<>();


        /*
        * 对于每个post&Tag 都把其中postId作为key value则对应生成一个LinkedList（不知道这里为什么用postId感觉又歧义---规范定义的是k这样
        * 这个add就会往value里面加值 虽然有一点不能理解 大概是这个作用
        *
        * 规范：
        * 如果指定的键还没有与值关联(或映射为空)，尝试使用给定的映射函数计算它的值，并将其输入到这个映射中，除非为空。
        * 如果映射函数返回null，则没有映射记录。 如果映射函数本身抛出(未检查)异常，则重新抛出异常，并且不记录映射。
        * 最常见的用法是构造一个新对象作为初始映射值或记忆结果，如:
        *   map.computeIfAbsent(key, k -> new Value(f(k)));
        * 或实现多值映射，map >，支持每个键多个值:
        *   map.computeIfAbsent(key, k -> new HashSet<V>()).add(v);
        *
        * 所以这里就需要多值映射 因为每个postId可以对应多个tag
        * */
        // Foreach and collect
        postTags.forEach(
            postTag -> tagListMap.computeIfAbsent(postTag.getPostId(), postId -> new LinkedList<>())
                .add(tagMap.get(postTag.getTagId())));

        return tagListMap;
    }


    @Override
    public List<Post> listPostsBy(Integer tagId) {
        Assert.notNull(tagId, "Tag id must not be null");

        // Find all post ids
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<Post> listPostsBy(Integer tagId, PostStatus status) {
        Assert.notNull(tagId, "Tag id must not be null");
        Assert.notNull(status, "Post status must not be null");

        // Find all post ids
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId, status);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<Post> listPostsBy(String slug, PostStatus status) {
        Assert.notNull(slug, "Tag slug must not be null");
        Assert.notNull(status, "Post status must not be null");

        Tag tag = tagRepository.getBySlug(slug)
            .orElseThrow(() -> new NotFoundException("查询不到该标签的信息").setErrorData(slug));

        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tag.getId(), status);

        return postRepository.findAllById(postIds);
    }

    @Override
    public Page<Post> pagePostsBy(Integer tagId, Pageable pageable) {
        Assert.notNull(tagId, "Tag id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all post ids
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId);

        return postRepository.findAllByIdIn(postIds, pageable);
    }

    @Override
    public Page<Post> pagePostsBy(Integer tagId, PostStatus status, Pageable pageable) {
        Assert.notNull(tagId, "Tag id must not be null");
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all post ids
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId, status);

        return postRepository.findAllByIdIn(postIds, pageable);
    }

    @Override
    public List<PostTag> mergeOrCreateByIfAbsent(Integer postId, Set<Integer> tagIds) {
        Assert.notNull(postId, "Post id must not be null");

        if (CollectionUtils.isEmpty(tagIds)) {
            return Collections.emptyList();
        }

        // Create post tags
        List<PostTag> postTagsStaging = tagIds.stream().map(tagId -> {
            // Build post tag
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tagId);
            return postTag;
        }).collect(Collectors.toList());

        List<PostTag> postTagsToRemove = new LinkedList<>();
        List<PostTag> postTagsToCreate = new LinkedList<>();

        List<PostTag> postTags = postTagRepository.findAllByPostId(postId);

        /*
        * postTagsStaging ：这次操作中添加的tag 联系的postTag
        * postTags：这是数据库里的
        * postTagsToRemove
        * postTagsToCreate
        *
        * 如果postTagsStaging中不存在postTags 就加入postTagsToRemove列表中（postTag过期
        * 反过来如果postTags中不存在postTagsStaging 就加入postTagsToCreate列表中（新建postTag
        *
        * 感觉有的复杂的逻辑 但是就是更新postTags
        * */
        postTags.forEach(postTag -> {
            if (!postTagsStaging.contains(postTag)) {
                postTagsToRemove.add(postTag);
            }
        });

        postTagsStaging.forEach(postTagStaging -> {
            if (!postTags.contains(postTagStaging)) {
                postTagsToCreate.add(postTagStaging);
            }
        });

        // Remove post tags
        removeAll(postTagsToRemove);

        // Remove all post tags need to remove
        postTags.removeAll(postTagsToRemove);

        // Add all created post tags
        postTags.addAll(createInBatch(postTagsToCreate));

        // Return post tags
        return postTags;
    }

    @Override
    public List<PostTag> listByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postTagRepository.findAllByPostId(postId);
    }

    @Override
    public List<PostTag> listByTagId(Integer tagId) {
        Assert.notNull(tagId, "Tag id must not be null");

        return postTagRepository.findAllByTagId(tagId);
    }

    @Override
    public Set<Integer> listTagIdsByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postTagRepository.findAllTagIdsByPostId(postId);
    }

    @Override
    public List<PostTag> removeByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postTagRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostTag> removeByTagId(Integer tagId) {
        Assert.notNull(tagId, "Tag id must not be null");

        return postTagRepository.deleteByTagId(tagId);
    }
}
