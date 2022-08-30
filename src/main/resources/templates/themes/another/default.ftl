<#macro default title,canonical,body_class>
<!DOCTYPE html>
<html lang="zh-CN">
<head>

    <#-- Document Settings -->
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />

    <#-- Base Meta -->
    <title>${title}</title>
    <meta name="HandheldFriendly" content="True" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />

    <#-- Styles'n'Scripts -->
    <link rel="stylesheet" type="text/css" href="${theme_base!}/assets/built/screen.css" />

    <#-- This tag outputs SEO meta+structured data and other important settings -->
    <meta name="keywords" content="${meta_keywords!}" />
    <meta name="description" content="${meta_description!}" />
    <@global.head />
    <link rel="canonical" href="${canonical}" />
    <meta name="referrer" content="no-referrer-when-downgrade" />

    <meta property="og:site_name" content="${title}" />
    <meta property="og:type" content="website" />
    <meta property="og:title" content="${title}" />
    <meta property="og:description" content="${meta_description!}" />
    <meta property="og:url" content="${canonical}" />
    <meta property="og:image" content="${settings.cover!'${theme_base!}/assets/images/blog-cover.jpg'}" />
    <meta property="article:publisher" content="https://www.facebook.com/ghost" />
    <meta name="twitter:card" content="summary_large_image" />
    <meta name="twitter:title" content="${title}" />
    <meta name="twitter:description" content="${meta_description!}" />
    <meta name="twitter:url" content="${canonical}" />
    <meta name="twitter:image" content="${settings.cover!'${theme_base!}/assets/images/blog-cover.jpg'}" />
    <meta name="twitter:site" content="@tryghost" />
    <meta property="og:image:width" content="2000" />
    <meta property="og:image:height" content="666" />
    <link rel="alternate" type="application/rss+xml" title="${blog_title!}" href="${rss_url!}" />

    <#if post??>
    <link rel="stylesheet" type="text/css" href="${theme_base!}/assets/prism/css/prism-${settings.code_pretty!'Default'}.css" />
    <script type="text/javascript" src="${theme_base!}/assets/prism/js/prism.js"></script>
    </#if>

</head>
<body class="${body_class}">

    <div class="site-wrapper">

        <#-- All the main content gets inserted here, index.hbs, post.hbs, etc -->
        <#nested >
        <#-- The footer at the very bottom of the screen -->
        <footer class="site-footer outer">
            <div class="site-footer-content inner">
                <section class="copyright">
                    <a href="${blog_url!}">${blog_title!}</a> &copy; ${.now?string('yyyy')}
                    <@global.footer_info />
                </section>
                <nav class="site-footer-nav">
                    <a href="${blog_url!}">Latest Posts</a>
                    <a href="https://www.facebook.com/ghost" target="_blank" rel="noopener">Facebook</a>
                    <a href="https://twitter.com/tryghost" target="_blank" rel="noopener">Twitter</a>
                    <a href="https://ghost.org" target="_blank" rel="noopener">Ghost</a>
                    <a href="https://halo.run" target="_blank" rel="noopener">Halo</a>
                </nav>
            </div>
        </footer>

    </div>

    <#-- The big email subscribe modal content -->

    <script>
        var images = document.querySelectorAll('.kg-gallery-image img');
        images.forEach(function (image) {
            var container = image.closest('.kg-gallery-image');
            var width = image.attributes.width.value;
            var height = image.attributes.height.value;
            var ratio = width / height;
            container.style.flex = ratio + ' 1 0%';
        })
    </script>


    <#-- jQuery + Fitvids, which makes all video embeds responsive -->
    <script src="${theme_base!}/assets/built/jquery.min.js"></script>
    <script type="text/javascript" src="${theme_base!}/assets/built/jquery.fitvids.js"></script>
</#macro>

<#macro scripts>
    <#nested >
    <@global.statistics />
</body>
</html>
</#macro>
