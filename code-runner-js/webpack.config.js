const webpack = require('webpack');
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin')
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = function(environment, options) {
    const env = process.env.NODE_ENV || 'development';
    const isProduction = env === 'production';

    return {
        entry: {
            babelPolyfill: 'babel-polyfill',
            app: './src/client/app.js',
        },
        output: {
            path: path.resolve(__dirname, 'dist/public'),
            filename: '[name].bundle.js',
            publicPath: '/public/'
        },
        devtool: isProduction ? 'none' : 'source-map',
        mode: env,
        module: {
            rules: [
                {
                    test: /\.(js|jsx)$/,
                    exclude: /node_modules/,
                    use: {
                        loader: 'babel-loader'
                    }
                },
                {
                    test: /\.css$/i,
                    use: [
                        MiniCssExtractPlugin.loader,
                        'css-loader'
                    ]
                }
            ]
        },
        resolve: {
            extensions: ['.js', '.jsx', '.css']
        },
        plugins: [
            new HtmlWebpackPlugin({
                title: 'Competition',
                hash: true,
                template: './src/client/index.html',
                chunks: ['babelPolyfill', 'app'],
                filename: './index.html'
            }),
            new MiniCssExtractPlugin({
                filename: "[name].css",
                chunkFilename: "[id].css"
             })
        ],
        watch: true
    };
}
