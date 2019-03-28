# emoji-to-text
Convert unicode emojis included in a given text, to their translated emoji description.

This basic app includes the EmojiToText class that you can call as simple as:

  String translated_text = EmojiToText.translateEmoji(context, input_text);

Our main goal is having as many languages as possible for all the emoji descriptions. That's why we are now using the descriptions translated from Unicode CLDR project v34 (see assets directory). Copyright © 1991-2019 Unicode, Inc. All rights reserved. You can see full license here: http://unicode.org/copyright.html#Exhibit1
We added some classes and methods in EmojiActivity to import this descriptions to an Android XML strings file format. 

For any doubt write us translations@codefactoryglobal.com

Where this come from? Having the translation of emoji may be very helpful in many situations, but if we think on accessibility it gets really important, as the only way for many people to know what's behind a funny image.

