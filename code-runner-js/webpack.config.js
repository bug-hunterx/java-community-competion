const webpack = require('webpack');
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin')
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = function(environment, options) {
    const env = process.env.NODE_ENV || 'development';
    const isProduction = env === 'production';

    return {
        entry: {
            app: './src/client/app.js',
            login: './src/client/login.js'
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
                chunks: ['app'],
                filename: './index.html'
            }),
            new HtmlWebpackPlugin({
                title: 'Login Page',
                hash: true,
                template: './src/client/login.html',
                chunks: ['login'],
                filename: './login.html'
            }),
            new MiniCssExtractPlugin({
                filename: "[name].css",
                chunkFilename: "[id].css"
             })
        ],
        watch: true
    };
}
